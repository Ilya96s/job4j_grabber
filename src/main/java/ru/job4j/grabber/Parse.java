package ru.job4j.grabber;

import java.io.IOException;
import java.util.List;

/**
 * Интерфейс описывающий парсинг сайта
 *
 * @author Ilya Kaltygin
 * @version 1.0
 */
public interface Parse {
    List<Post> list(String link) throws IOException;
}
