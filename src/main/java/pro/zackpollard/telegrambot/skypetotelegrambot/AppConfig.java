package pro.zackpollard.telegrambot.skypetotelegrambot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.skypetotelegrambot.managers.SkypeManager;
import pro.zackpollard.telegrambot.skypetotelegrambot.managers.SkypeManagerKeeper;

@Configuration
@ComponentScan
@PropertySource("classpath:config.properties")
class AppConfig {

    @Value("${telegram.botKey}")
    private String botKey;

    @Autowired
    private SkypeManagerKeeper skypeManagerKeeper;

    @Bean
    TelegramBot telegramBot() {
        return TelegramBot.login(botKey);
    }

    @Bean
    SkypeManager skypeManager() {
        return skypeManagerKeeper.getSkypeManager();
    }

    @Bean
    SkypeToTelegramBot skypeToTelegramBot() {
        return new SkypeToTelegramBot();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
