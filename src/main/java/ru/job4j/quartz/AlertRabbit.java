package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * Знакомство с библиотекой Quartz. Выполнение действий с периодичностью.
 *
 * @author Ilya Kaltygin
 * @version 1.1
 */

public class AlertRabbit {
    public static void main(String[] args) {
        try {
            /* Конфигурирование */
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            /* Создание задачи */
            JobDetail job = newJob(Rabbit.class).build();
            /* Создание расписания и настройка периодичности запуска */
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(
                            readProperties("rabbit.properties")
                            .getProperty("rabbit.interval")))
                    .repeatForever();
            /* Создание триггера и указание когда должен он должен запуститься */
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            /* Загрузка задачи и триггера в планировщик */
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static Properties readProperties(String properties) {
        Properties cfg = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(properties)) {
            cfg.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfg;
    }


    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}