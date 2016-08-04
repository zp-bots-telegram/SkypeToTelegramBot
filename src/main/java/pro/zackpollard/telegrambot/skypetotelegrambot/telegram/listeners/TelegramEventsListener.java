package pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.GroupChat;
import pro.zackpollard.telegrambot.api.chat.message.content.type.PhotoSize;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.PhotoMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.skypetotelegrambot.managers.SkypeManager;
import pro.zackpollard.telegrambot.skypetotelegrambot.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Zack Pollard
 */
@Component
public class TelegramEventsListener implements Listener {

    public static Logger logger = LoggerFactory.getLogger(TelegramEventsListener.class);

    @Autowired
    private SkypeManager skypeManager;
    @Autowired
    private TelegramBot telegramBot;

    public TelegramEventsListener() {
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {

        if (event.getChat().getType().equals(ChatType.GROUP)) {

            Chat chat = skypeManager.getSkypeChat(event.getChat());

            if (chat != null) {

                try {
                    ChatMessage message = chat.sendMessage(Message.create().with(Text.plain(event.getContent().getContent())));
                    skypeManager.getLastSyncedSkypeMessage().put(chat.getIdentity(), message.getId());
                } catch (SkypeException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {

                Map<String, String> chats = skypeManager.getLinkingQueue().get(event.getChat().getId());

                if (chats != null) {

                    String chatID = chats.get(event.getContent().getContent());

                    if (chatID != null) {

                        skypeManager.createLink(event.getMessage().getSender().getId(), (GroupChat) event.getChat(), skypeManager.getSkype(event.getMessage().getSender()).getChat(chatID));
                    }
                } else {

                    event.getChat().sendMessage("This chat is not linked to a skype chat, use /link to link it!");
                }
            }
        }
    }

    @Override
    public void onPhotoMessageReceived(PhotoMessageReceivedEvent event) {

        if (event.getChat().getType().equals(ChatType.GROUP)) {

            Chat chat = skypeManager.getSkypeChat(event.getChat());

            if (chat != null) {

                String imgurID = skypeManager.getImgurID(event.getMessage().getSender().getId());

                if (imgurID != null) {

                    int largest = 0;
                    int largestDimension = 0;

                    for (int i = 0; i < event.getContent().getContent().length; ++i) {

                        PhotoSize content = event.getContent().getContent()[i];
                        int dimension = content.getWidth() * content.getHeight();

                        if (dimension > largestDimension) {

                            largest = i;
                            largestDimension = dimension;
                        }
                    }

                    File file = null;

                    try {
                        file = File.createTempFile(System.currentTimeMillis() + "-skypetg", "png");
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }

                    event.getContent().getContent()[largest].downloadFile(telegramBot, file);

                    String imgurURL = null;

                    try {
                        imgurURL = Utils.uploadToImgur(file, imgurID);
                    } catch (UnirestException e) {
                        logger.error(e.getMessage(), e);
                    }
                    //TODO: Add code for uploading the image to imgur

                    try {

                        ChatMessage message = chat.sendMessage(Message.create().with(Text.plain(imgurURL + "\n" + event.getContent().getCaption())));
                    } catch (SkypeException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else {

                    event.getChat().sendMessage(SendableTextMessage.builder().replyTo(event.getMessage()).message("Photo sending is not supported without adding an imgur API key. Do this with the command /setimgurid IMGURID").build());
                }
            }
        }
    }
}