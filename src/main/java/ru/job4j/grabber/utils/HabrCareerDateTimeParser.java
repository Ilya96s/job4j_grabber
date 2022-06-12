package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Преобразования строки в дату
 *
 * @author Ilya Kaltygin
 * @version 1.0
 */
public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss").ISO_OFFSET_DATE_TIME);
    }
}
