package pro.zackpollard.telegrambot.skypetotelegrambot.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * @author Zack Pollard
 */
@Component
public class SkypeManagerKeeper {

    public static Logger logger = LoggerFactory.getLogger(SkypeManagerKeeper.class);

    @Value("${skype.manager.filepath}")
    private String storeFilepath;

    private File skypeManagerFile;

    public SkypeManager getSkypeManager() {

        SkypeManager skypeManager;

        skypeManagerFile = new File(storeFilepath);

        if (skypeManagerFile.exists()) {

            skypeManager = this.loadSkypeManager();
        } else {

            try {
                skypeManagerFile.createNewFile();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            skypeManager = new SkypeManager();

            this.saveSkypeManager(skypeManager);
        }

        if (skypeManager == null) {
            logger.error("The save file could not be loaded. Either fix the save file or delete it and restart the bot.");
            System.exit(1);
        }
        return skypeManager;
    }

    public SkypeManager loadSkypeManager() {

        SkypeManager loadedSaveFile;

        try (Reader reader = new InputStreamReader(new FileInputStream(skypeManagerFile), "UTF-8")) {

            Gson gson = new GsonBuilder().create();
            loadedSaveFile = gson.fromJson(reader, SkypeManager.class);

            return loadedSaveFile;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public boolean saveSkypeManager(SkypeManager skypeManager) {

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
            logger.error("The config could not be saved as the file couldn't be found on the storage device. Please check the directories read/write permissions and contact the developer!", e);
        } catch (IOException e) {
            logger.error("The config could not be written to as an error occured. Please check the directories read/write permissions and contact the developer!", e);
        }

        return false;
    }
}