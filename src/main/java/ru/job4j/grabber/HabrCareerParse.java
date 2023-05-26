package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            String anotherPageLink = PAGE_LINK + i;
            Connection connection = Jsoup.connect(anotherPageLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String date = dateElement.attr("datetime");
                String description;
                try {
                    description = new HabrCareerParse().retrieveDescription(link);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.printf("%s %s %s %s%n", date, vacancyName, link, description);

            });
        }
    }

    private String retrieveDescription(String link) throws IOException {
        String description = "";
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        String title = document.select(".section-title__title").first().text();
        description += title + "\n";
        Elements rows = document.select(".vacancy-description__text");
        for (Element row : rows) {
            for (int i = 0; i < row.childNodeSize(); i++) {
                description += row.child(i).text() + "\n";
            }
        }
        return description;
    }
}

