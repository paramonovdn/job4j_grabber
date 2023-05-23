package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    private static int rabbitInterval;

    private static Connection connection;
    public static void main(String[] args) throws SQLException {
        try  {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
           Class.forName(getRabbitProperty().getProperty("driver-class-name"));
            connection = DriverManager.getConnection(getRabbitProperty().getProperty("url"),
                    getRabbitProperty().getProperty("username"), getRabbitProperty().getProperty("password"));
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
            rabbitInterval = Integer.parseInt(getRabbitProperty().getProperty("rabbit.interval"));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(rabbitInterval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SchedulerException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            connection.close();
        }
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = connection.prepareStatement("INSERT INTO rabbit(created_date) VALUES (?)")) {
                statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static Properties getRabbitProperty() {
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties config = new Properties();
            config.load(in);
            return config;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}