package pro.zackpollard.telegrambot.skypetotelegrambot.skype.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.samczsun.skype4j.chat.IndividualChat;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageEditedEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.PictureReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.TypingReceivedEvent;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.*;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.skypetotelegrambot.utils.Utils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zack Pollard
 */
public class SkypeEventsListener implements Listener {

    private final SkypeToTelegramBot instance;
    private final TelegramBot telegramBot;
    private final Long telegramID;
    private final File tmpImageDirectory;

    //<SkypeChatID, <SkypeMessageID, TGMessageToChat>>
    private final Map<String, Cache<String, TGMessageToChat>> chatCache;

    public SkypeEventsListener(SkypeToTelegramBot instance, TelegramBot telegramBot, Long telegramID) {

        this.instance = instance;
        this.telegramBot = telegramBot;
        this.telegramID = telegramID;
        this.chatCache = new HashMap<>();
        this.tmpImageDirectory = new File(System.getProperty("java.io.tmpdir") + File.separatorChar + "tgtoskypebot" + System.currentTimeMillis());
        this.tmpImageDirectory.mkdirs();
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {

        System.out.println("Person is typing in " + event.getChat().getIdentity());
        System.out.println("Chat " + (event.getChat().isLoaded() ? "is" : "isn't") + " loaded.");

        try {
            if(!event.getChat().isLoaded()) {
                instance.getSkypeManager().getSkype(telegramID).loadChat(event.getChat().getIdentity());
            }
        } catch (ConnectionException | ChatNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("Chat " + (event.getChat().isLoaded() ? "is" : "isn't") + " now loaded.");

        String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

        if(chat != null) {

            if(!event.getMessage().getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                Message message = null;
                try {
                    message = telegramBot.sendMessage(TelegramBot.getChat(chat), SendableTextMessage.builder().message("*" + (event.getMessage().getSender().getDisplayName() != null ? event.getMessage().getSender().getDisplayName() : event.getMessage().getSender().getUsername()) + "*: " + Utils.escapeMarkdownText(event.getMessage().getContent().asPlaintext())).parseMode(ParseMode.MARKDOWN).build());
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }

                if (message != null) {

                    Cache<String, TGMessageToChat> messageCache = chatCache.get(event.getChat().getIdentity());

                    if (messageCache == null) {

                        messageCache = CacheBuilder.newBuilder().expireAfterWrite(45, TimeUnit.MINUTES).build();
                        chatCache.put(event.getChat().getIdentity(), messageCache);
                    }

                    messageCache.put(event.getMessage().getId(), new TGMessageToChat(message.getChat().getId(), message));
                    instance.getSkypeManager().getLastSyncedSkypeMessage().put(event.getChat().getIdentity(), event.getMessage().getId());
                    instance.saveSkypeManager();
                }
            }
        }
    }

    @EventHandler
    public void onTyping(TypingReceivedEvent event) {

        if(event.isTyping() && event.getChat() instanceof IndividualChat) {

            String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

            if(!event.getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                if (chat != null) {

                    telegramBot.sendMessage(TelegramBot.getChat(chat), SendableChatAction.builder().chatAction(ChatAction.TYPING_TEXT_MESSAGE).build());
                }
            }
        }
    }

    @EventHandler
    public void onMessageEdit(MessageEditedEvent event) {

        String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

        if(chat != null) {

            if(!event.getMessage().getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                System.out.println("Message didn't have same username");

                Cache<String, TGMessageToChat> skypeChatCache = chatCache.get(event.getChat().getIdentity());

                if(skypeChatCache != null) {

                    System.out.println("Cache wasn't null");

                    TGMessageToChat tgMessageToChat = skypeChatCache.getIfPresent(event.getMessage().getId());

                    if(tgMessageToChat != null) {

                        try {
                            TelegramBot.getChat(chat).sendMessage(SendableTextMessage.builder().message(
                                    "_Message Edited_\n*" + (event.getMessage().getSender().getDisplayName() != null ? event.getMessage().getSender().getDisplayName() : event.getMessage().getSender().getUsername()) + "*: " + Utils.escapeMarkdownText(event.getNewContent())).replyTo(tgMessageToChat.getTgMessage()).parseMode(ParseMode.MARKDOWN).build(), telegramBot);
                            instance.getSkypeManager().getLastSyncedSkypeMessage().put(event.getChat().getIdentity(), event.getMessage().getId());
                        } catch (ConnectionException e) {
                            e.printStackTrace();
                        }

                        instance.saveSkypeManager();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPictureRecevied(PictureReceivedEvent event) {

        System.out.println("Picture received!");

        String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

        if(chat != null) {

            if(!event.getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                File imageFile = new File(tmpImageDirectory + File.separator + event.getOriginalName());

                try {
                    ImageIO.write(event.getSentImage(), "png", imageFile);
                } catch (IOException e) {
                    System.err.println("An error occured whilst trying to save an image sent from skype to disk.");
                    TelegramBot.getChat(chat).sendMessage(SendableTextMessage.builder().message("*[ERROR]* - An image that was sent from skype could not be sent successfully to telegram, report this to @zackpollard.").parseMode(ParseMode.MARKDOWN).build(), telegramBot);
                    e.printStackTrace();
                    return;
                }

                try {
                    TelegramBot.getChat(chat).sendMessage(SendablePhotoMessage.builder().photo(new InputFile(imageFile)).caption("Image sent by " + (event.getSender().getDisplayName() != null ? event.getSender().getDisplayName() : event.getSender().getUsername())).build(), telegramBot);
                } catch (ConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class TGMessageToChat {

        @Getter
        private final String tgChat;
        @Getter
        private final Message tgMessage;

        public TGMessageToChat(String tgChat, Message tgMessage) {

            this.tgChat = tgChat;
            this.tgMessage = tgMessage;
        }
    }
}