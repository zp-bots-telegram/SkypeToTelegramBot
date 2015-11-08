package pro.zackpollard.telegrambot.skypetotelegrambot.skype.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageEditedEvent;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.TypingReceivedEvent;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.ChatAction;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableChatAction;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zack Pollard
 */
public class SkypeEventsListener implements Listener {

    private final SkypeToTelegramBot instance;
    private final TelegramBot telegramBot;
    private final Integer telegramID;

    //<SkypeChatID, <SkypeMessageID, TGMessageToChat>>
    private final Map<String, Cache<String, TGMessageToChat>> chatCache;

    public SkypeEventsListener(SkypeToTelegramBot instance, TelegramBot telegramBot, Integer telegramID) {

        this.instance = instance;
        this.telegramBot = telegramBot;
        this.telegramID = telegramID;
        this.chatCache = new HashMap<>();
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {

        String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

        if(chat != null) {

            if(!event.getMessage().getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                Message message = telegramBot.sendMessage(TelegramBot.getChat(chat), SendableTextMessage.builder().message("*" + (event.getMessage().getSender().getDisplayName() != null ? event.getMessage().getSender().getDisplayName() : event.getMessage().getSender().getUsername()) + "*: " + event.getMessage().getContent().asPlaintext()).parseMode(ParseMode.MARKDOWN).build());

                if (message != null) {

                    Cache<String, TGMessageToChat> messageCache = chatCache.get(event.getChat().getIdentity());

                    if (messageCache == null) {

                        messageCache = CacheBuilder.newBuilder().expireAfterWrite(45, TimeUnit.MINUTES).build();
                        chatCache.put(event.getChat().getIdentity(), messageCache);
                    }

                    messageCache.put(event.getMessage().getId(), new TGMessageToChat(message.getChat().getId(), message));
                }
            }
        }
    }

    @EventHandler
    public void onTyping(TypingReceivedEvent event) {

        if(event.isTyping()) {

            String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

            if(!event.getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                if (chat != null) {

                    telegramBot.sendMessage(TelegramBot.getChat(chat), new SendableChatAction(ChatAction.TYPING_TEXT_MESSAGE));
                }
            }
        }
    }

    @EventHandler
    public void onMessageEdit(MessageEditedEvent event) {

        System.out.println("Message Edited: " + event.getNewContent());
        System.out.println(event.getChat());
        System.out.println(instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID));
        System.out.println(event.getMessage().getId());
        System.out.println(event.getMessage().getContent());

        String chat = instance.getSkypeManager().getTelegramChat(event.getChat(), telegramID);

        System.out.println("Telegram chat ID was: " + chat);

        if(chat != null) {

            if(!event.getMessage().getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())) {

                System.out.println("Message didn't have same username");

                Cache<String, TGMessageToChat> skypeChatCache = chatCache.get(event.getChat().getIdentity());

                if(skypeChatCache != null) {

                    System.out.println("Cache wasn't null");

                    TGMessageToChat tgMessageToChat = skypeChatCache.getIfPresent(event.getMessage().getId());

                    if(tgMessageToChat != null) {

                        TelegramBot.getChat(chat).sendMessage(SendableTextMessage.builder().message(
                                "_Message Edited_\n*" + (event.getMessage().getSender().getDisplayName() != null ? event.getMessage().getSender().getDisplayName() : event.getMessage().getSender().getUsername()) + "*: " + event.getNewContent()).replyTo(tgMessageToChat.getTgMessage()).parseMode(ParseMode.MARKDOWN).build(), telegramBot);
                    }
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