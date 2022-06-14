package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Периодический запуск парсера с использованием quartz
 */
public interface Grab {
    void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException;
}
