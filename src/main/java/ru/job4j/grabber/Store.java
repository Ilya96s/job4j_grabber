package ru.job4j.grabber;

import java.util.List;

/**
 * Хранилище. Осуществляет связь с базой
 */
public interface Store {

    void save(Post post);

    List<Post> getAll();

    Post findById(int id);
}
