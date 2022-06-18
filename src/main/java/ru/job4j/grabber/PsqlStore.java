package ru.job4j.grabber;

import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Класс хранилища в базе данных
 *
 * @author Ilya Kaltygin
 */

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    private static final String PAGE_LINK = "https://career.habr.com/vacancies/java_developer?page=";

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
         try {
             cnn = DriverManager.getConnection(
                     cfg.getProperty("jdbc.url"),
                     cfg.getProperty("jdbc.username"),
                     cfg.getProperty("jdbc.password"));
         } catch (SQLException e) {
             throw new IllegalArgumentException(e);
         }
    }

    /**
     * Метод сохраняет объявление в базу данных
     * @param post объявление
     */
    @Override
    public void save(Post post) {
        try (PreparedStatement statement = cnn.prepareStatement(
                "insert into post (name, text, link, created) values (?, ?, ?, ?) on conflict (link) do nothing")) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод получает все объявления из базы данных
     * @return список объявлений
     */
    @Override
    public List<Post> getAll() {
        List<Post> postList = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("select * from post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    postList.add(getPostFromResultSet(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return postList;
    }

    /**
     * Поиск объявленияв базе данных по идентификатору объявления
     * @param id идентификатор обявления
     * @return объявление найденое по идентификатору
     */
    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement = cnn.prepareStatement("select * from post where id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = getPostFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return post;
    }

    /**
     * Получение объекта Post из ResultSet
     * @param resultSet объект ResultSet
     * @return объект Post
     */
    private static Post getPostFromResultSet(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime());
    }

    /**
     * Загрузка настроек
     * @param properties файл с настройками
     * @return объект Properties
     */
    private static Properties getProperties(String properties) {
        Properties cfg = new Properties();
        try (InputStream in = PsqlStore.class.getClassLoader().getResourceAsStream(properties)) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    /**
     * Закрытие ресурсов Connection
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public static void main(String[] args) {
       try (PsqlStore psqlStore = new PsqlStore(getProperties("grabber.properties"))) {
           DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
           HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
           List<Post> parseList = habrCareerParse.list(PAGE_LINK);
           for (Post post : parseList) {
               psqlStore.save(post);
           }
           List<Post> getAllList = psqlStore.getAll();
           for (Post post : getAllList) {
               System.out.println(post);
           }
           System.out.println(psqlStore.findById(22));
       } catch (Exception e) {
           e.printStackTrace();
       }
    }
}
