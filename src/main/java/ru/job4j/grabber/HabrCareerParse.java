package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);
    private final DateTimeParser dateTimeParser;
    public HabrCareerParse(DateTimeParser dateTimeParser) {
      this.dateTimeParser = dateTimeParser;
    }


    private String retrieveDescription(String link) throws IOException {
        StringBuilder description = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        String title = document.select(".section-title__title").first().text();
        description.append(title + "\n");
        Elements rows = document.select(".vacancy-description__text");
        for (Element row : rows) {
            for (int i = 0; i < row.childNodeSize(); i++) {
                description.append(row.child(i).text() + "\n");
            }
        }
        return description.toString();
    }
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            Element dateElement = row.select(".vacancy-card__date").first().child(0);
            String vacancyName = titleElement.text();
            String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String dateTime = dateElement.attr("datetime");
            LocalDateTime vacancyDate = dateTimeParser.parse(dateTime);
            String vacancyDescription;
            try {
                vacancyDescription = retrieveDescription(vacancyLink);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            postList.add(new Post(0, vacancyName, vacancyLink, vacancyDescription, vacancyDate));
        });
        return postList;
    }
}

