package ru.job4j.quartz;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Парсинг HTML страницы https://career.habr.com/vacancies/java_developer
 *
 * @author Ilya Kaltygin
 * @version 1.0
 */
public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        Connection connection = Jsoup.connect(PAGE_LINK);
        /* Получение страницы для дальнейшей работы с ней */
        Document document = connection.get();
        /* Получение всех вакансий на странице */
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            /* Получение названия вакансии */
            Element titleElement = row.select(".vacancy-card__title").first();
            /* Получение ссылки */
            Element linkElement = titleElement.child(0);
            /* Получение даты создания вакансии */
            Element dateElement = row.select(".vacancy-card__date").first().child(0);
            String vacancyDate = dateElement.attr("datetime");
            String vacancyName = titleElement.text();
            String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            System.out.printf("%s : %s : %s%n", vacancyName, link, vacancyDate);
        });
    }
}
