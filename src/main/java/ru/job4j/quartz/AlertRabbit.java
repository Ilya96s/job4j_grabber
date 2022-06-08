package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * Выполнение действий с периодичностью. Job с параметрами
 *
 * @author Ilya Kaltygin
 * @version 1.0
 */

public class AlertRabbit {
    public static void main(String[] args) {
        Properties cfg = getProperties("src/main/resources/rabbit.properties");
        try (Connection cn = getConnection(cfg)) {
            /* Конфигурирование */
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            /* Дополнительная инфомация для задачи */
            JobDataMap data = new JobDataMap();
            data.put("connection", cn);
            /* Создание задачи */
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            /* Создание расписания */
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(getInterval(cfg))
                    .repeatForever();
            /* Задача выполняется через триггер */
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            /* Загрузка задачи и триггера в планировщик */
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(5000);
            scheduler.shutdown();
        } catch (ClassNotFoundException | SchedulerException | SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загрузка файла конфигурации
     * @param path имя файла конфигурации
     * @return Объект Properties
     */
    public static Properties getProperties(String path) {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(path)) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cfg;
    }

    /**
     * Инициализация подключения к базе данных
     * @param cfg файл с конфигурацией
     * @return Объект Connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static Connection getConnection(Properties cfg) throws SQLException, ClassNotFoundException {
        Class.forName(cfg.getProperty("jdbc.driver"));
        return DriverManager.getConnection(
                cfg.getProperty("jdbc.url"),
                cfg.getProperty("jdbc.username"),
                cfg.getProperty("jdbc.password"));

        }

        /**
     * Метод считывает интервал из файла конфигурации
     * @param cfg файл с настройкми
     * @return интервал
     */
        public static int getInterval(Properties cfg) {
        return Integer.parseInt(cfg.getProperty("rabbit.interval"));
        }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement statement = cn.prepareStatement("insert into rabbit(created_date) values(?);")) {
                statement.setLong(1, System.currentTimeMillis());
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}