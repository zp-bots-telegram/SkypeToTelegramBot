package pro.zackpollard.telegrambot.skypetotelegrambot.storage;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class SkypeToTelegramChat {

    @Getter
    private final Map<String, String> map;

    public SkypeToTelegramChat() {

        map = new HashMap<>();
    }
}
