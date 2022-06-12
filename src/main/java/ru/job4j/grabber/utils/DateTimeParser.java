package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

/**
 * @author Ilya Kaltygin
 * @version 1.0
 */
public interface DateTimeParser {
    LocalDateTime parse(String parse);
}
