package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Соединение всех частей программы в целое приложение
 *
 * @author Ilya Kaltygin
 */
public class Grabber implements Grab {

    private final Properties cfg = new Properties();

    private static final String PAGE_LINK = "https://career.habr.com/vacancies/java_developer?page=";

    /**
     * Метоод возвращает объекта типа Store
     */
    public Store store() {
        return new PsqlStore(cfg);
    }

    /**
     * Создание планировщика и его запуск
     * @return планировщик
     */
    public Scheduler scheduler() {
        Scheduler scheduler = null;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return scheduler;
    }

    /**
     * Загрузка настроек
     */
    public void cfg() {
        try (InputStream in = Grab.class.getClassLoader().getResourceAsStream("grabber.properties")) {
            cfg.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Периодический запуск парсера с использованием quartz
     * @param parse объект типа Parse
     * @param store объект типа Store
     * @param scheduler объект типа Scheduler
     */
    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) {
        try {
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            data.put("parse", parse);
            JobDetail job = newJob(GrabJob.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(cfg.getProperty("time")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод создает серверный сокет и при отправляет клиенту содержимое базы данных
     * @param store объект определяет связь с базой данных
     */
    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(cfg.getProperty("port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static class GrabJob implements Job {

        /**
         * Действия, которые будет выполнять планировщик
         * @param context объект содержит информацию, которая была передана заданию, перед тем как оно было запущено
         * @throws JobExecutionException
         */
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            List<Post> postList = parse.list(PAGE_LINK);
            postList.forEach(store::save);
            List<Post> getPostList = store.getAll();
            for (Post post : getPostList) {
                System.out.println(post);
            }
            System.out.println(store.findById(20));
        }
    }

    public static void main(String[] args) {
        Grabber grabber = new Grabber();
        grabber.cfg();
        Scheduler scheduler = grabber.scheduler();
        Store store = grabber.store();
        grabber.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grabber.web(store);
    }
}
