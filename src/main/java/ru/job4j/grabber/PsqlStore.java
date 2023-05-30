package ru.job4j.grabber;

import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import ru.job4j.quartz.AlertRabbit;

import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("driver-class-name"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("url"),
                    cfg.getProperty("username"),
                    cfg.getProperty("password")

            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement("INSERT INTO post(name, text, link, created)"
                         + "VALUES (?, ?, ?, ?) ON CONFLICT (link) DO NOTHING;",
                                +Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post;")) {
            try (ResultSet selection = statement.executeQuery()) {
                while (selection.next()) {
                    int id = selection.getInt(1);
                    String name = selection.getString(2);
                    String text = selection.getString(3);
                    String link = selection.getString(4);
                    Timestamp timeStamp = selection.getTimestamp(5);
                    LocalDateTime created = timeStamp.toLocalDateTime();
                    Post post = new Post(id, name, text, link, created);
                    postList.add(post);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return postList;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet selection = statement.executeQuery()) {
                if (selection.next()) {
                    String name = selection.getString(2);
                    String text = selection.getString(3);
                    String link = selection.getString(4);
                    Timestamp timeStamp = selection.getTimestamp(5);
                    LocalDateTime created = timeStamp.toLocalDateTime();
                    post = new Post(id, name, text, link, created);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
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

    public static void main(String[] args) throws Exception {
        PsqlStore psqlStore = new PsqlStore(getRabbitProperty());
        Post post1 = new Post(0, "Java developer", "http://somelink1", "some description1", LocalDateTime.parse("2023-05-22T16:36:12"));
        Post post2 = new Post(0, "QA- инженер", "http://somelink2", "some description2", LocalDateTime.parse("2023-05-21T17:26:13"));
        Post post3 = new Post(0, "Project manager", "http://somelink33", "some description3", LocalDateTime.parse("2023-05-20T15:46:22"));
        psqlStore.save(post1);
        psqlStore.save(post2);
        psqlStore.save(post3);

        System.out.println(psqlStore.findById(1));
        System.out.println("=============================");
        System.out.println(psqlStore.getAll());
        psqlStore.close();

    }
}