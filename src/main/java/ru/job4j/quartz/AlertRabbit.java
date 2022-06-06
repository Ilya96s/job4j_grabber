package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

/**
 * Знакомство с библиотекой Quartz. Выполнение действий с периодичностью.
 *
 * @author Ilya Kaltygin
 * @version 1.0
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
                    .withIntervalInSeconds(readProperties("rabbit.properties"))
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

    public static int readProperties(String properties) {
        Properties cfg = new Properties();
        String interval = null;
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(properties)) {
            cfg.load(in);
            interval = cfg.getProperty("rabbit.interval");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Integer.parseInt(interval);
    }


    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here ...");
        }
    }
}
