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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Парсинг HTML страницы https://career.habr.com/vacancies/java_developer
 *
 * @author Ilya Kaltygin
 * @version 1.0
 */
public class HabrCareerParse {

    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer?page=", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    /**
     * Загрузка деталей обявления
     * @param link ссылка на описание вакансии
     * @throws IOException
     */
    private String retrieveDescription(String link) throws IOException {
        Document document = Jsoup.connect(link).get();
        Elements element = document.select(".style-ugc");
        return element.text();
    }

    private Post retrievePost(Element titleElement, Element dateElement, Element linkElement) {
        String vacancyDate = dateElement.attr("datetime");
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String vacancyDescription = null;
        try {
            vacancyDescription = retrieveDescription(vacancyLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Post(vacancyName, vacancyLink, vacancyDescription, new HabrCareerDateTimeParser().parse(vacancyDate));
    }

    /**
     * Метод загружает список всех постов
     * @param link ссылка на страницу с вакансиями
     * @return список постов
     * @throws IOException
     */
    public List<Post> list(String link) throws IOException {
        List<Post> postList = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            /* Получение страницы для дальнейшей работы с ней */
            Document document = Jsoup.connect(link).get();
            /* Получение всех вакансий на странице */
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                /* Получение названия вакансии */
                Element titleElement = row.select(".vacancy-card__title").first();
                /* Получение ссылки */
                Element linkElement = titleElement.child(0);
                /* Получение даты создания вакансии */
                Element dateElement = row.select(".vacancy-card__date").first().child(0);
                postList.add(retrievePost(titleElement, dateElement, linkElement));
            });
        }
        for (Post p : postList) {
            System.out.println(p);
        }
        return postList;
    }

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= 5; i++) {
            Connection connection = Jsoup.connect(PAGE_LINK + i);
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
}
