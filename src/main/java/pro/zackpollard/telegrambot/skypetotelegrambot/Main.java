package pro.zackpollard.telegrambot.skypetotelegrambot;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(AppConfig.class);

        SkypeToTelegramBot bean = context.getBean(SkypeToTelegramBot.class);
        bean.start();
    }
}
