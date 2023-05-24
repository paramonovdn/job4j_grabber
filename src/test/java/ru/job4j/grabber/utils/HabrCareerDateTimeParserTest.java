package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

public class HabrCareerDateTimeParserTest {

    @Test
    public void randomTimeTest() {
        HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
        String dateTime = "2023-05-24T13:27:05+03:00";
        LocalDateTime expectedTime = LocalDateTime.parse("2023-05-24T13:27:05");
        assertThat(timeParser.parse(dateTime)).isEqualTo(expectedTime);
    }
}
