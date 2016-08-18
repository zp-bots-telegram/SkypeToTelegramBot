package pro.zackpollard.telegrambot.skypetotelegrambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.skypetotelegrambot.managers.SkypeManager;
import pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners.TelegramCommandListener;
import pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners.TelegramEventsListener;

/**
 * @author Zack Pollard
 */
public class SkypeToTelegramBot {

    public static Logger logger = LoggerFactory.getLogger(SkypeToTelegramBot.class);

    @Autowired
    private TelegramBot telegramBot;
    @Autowired
    private TelegramCommandListener telegramCommandListener;
    @Autowired
    private TelegramEventsListener telegramEventsListener;
    @Autowired
    private SkypeManager skypeManager;

    public void start() {
        try {
            logger.info("Starting...");
            skypeManager.postLoadInit();
            telegramBot.getEventsManager().register(telegramCommandListener);
            telegramBot.getEventsManager().register(telegramEventsListener);
            telegramBot.startUpdates(false);
            logger.info("End starting. Working...");
        } catch (Exception e) {
            logger.error("ERROR!", e);
        }
    }

}