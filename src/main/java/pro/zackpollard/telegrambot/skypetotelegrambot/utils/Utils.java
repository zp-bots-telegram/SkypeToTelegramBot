package pro.zackpollard.telegrambot.skypetotelegrambot.utils;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;

/**
 * @author Zack Pollard
 */
public class Utils {

    private static final String BASE_URL = "https://api.imgur.com/3/image.json";

    public static String escapeMarkdownText(String text) {

        return text.replace("*", "\\*").replace("_", "\\_").replace("`", "\\`");
    }

    public static String uploadToImgur(File file, String clientID) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest
                .post(BASE_URL)
                .header("Authorization", "Client-ID " + clientID)
                .field("image", file)
                .asJson();

        return (String) response.getBody().getObject().getJSONObject("data").get("link");
    }
}
