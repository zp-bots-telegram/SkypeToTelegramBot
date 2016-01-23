package pro.zackpollard.telegrambot.skypetotelegrambot.utils;

/**
 * @author Zack Pollard
 */
public class Utils {

    public static String escapeMarkdownText(String text) {

        return text.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }
}
