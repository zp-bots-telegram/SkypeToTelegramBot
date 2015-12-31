package pro.zackpollard.telegrambot.skypetotelegrambot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.skypetotelegrambot.managers.SkypeManager;
import pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners.TelegramCommandListener;
import pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners.TelegramEventsListener;

import java.io.*;

/**
 * @author Zack Pollard
 */
public class SkypeToTelegramBot {

    @Getter
    private final TelegramBot telegramBot;

    private final TelegramCommandListener telegramCommandListener;
    private final TelegramEventsListener telegramEventsListener;

    @Getter
    private static SkypeToTelegramBot instance;

    @Getter
    private SkypeManager skypeManager;
    private File skypeManagerFile;

    public SkypeToTelegramBot(String API_KEY) {

        instance = this;

        this.telegramBot = TelegramBot.login(API_KEY);

        this.telegramCommandListener = new TelegramCommandListener(this);
        this.telegramEventsListener = new TelegramEventsListener(this);

        initSkypeManager();

        skypeManager.postLoadInit();
    }

    public void start() {

        telegramBot.getEventsManager().register(telegramCommandListener);
        telegramBot.getEventsManager().register(telegramEventsListener);
        telegramBot.startUpdates(false);
    }

    public void initSkypeManager() {

        skypeManagerFile = new File("tgtoskypedata.json");

        if (skypeManagerFile.exists()) {

            skypeManager = this.loadSkypeManager();
        } else {

            try {
                skypeManagerFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            skypeManager = new SkypeManager();

            this.saveSkypeManager();
        }

        if (skypeManager == null) {

            System.err.println("The save file could not be loaded. Either fix the save file or delete it and restart the bot.");
            System.exit(1);
        }
    }

    private SkypeManager loadSkypeManager() {

        SkypeManager loadedSaveFile;

        try (Reader reader = new InputStreamReader(new FileInputStream(skypeManagerFile), "UTF-8")) {

            Gson gson = new GsonBuilder().create();
            loadedSaveFile = gson.fromJson(reader, SkypeManager.class);

            return loadedSaveFile;
        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }

    public boolean saveSkypeManager() {

        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        Gson gson = builder.create();
        String json = gson.toJson(skypeManager);

        FileOutputStream outputStream;

        try {

            outputStream = new FileOutputStream(skypeManagerFile);
            outputStream.write(json.getBytes());
            outputStream.close();

            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.err.println("The config could not be saved as the file couldn't be found on the storage device. Please check the directories read/write permissions and contact the developer!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("The config could not be written to as an error occured. Please check the directories read/write permissions and contact the developer!");
        }

        return false;
    }
}