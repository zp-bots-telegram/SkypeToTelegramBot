package pro.zackpollard.telegrambot.skypetotelegrambot.skype.listeners;

import com.samczsun.skype4j.events.EventHandler;
import com.samczsun.skype4j.events.Listener;
import com.samczsun.skype4j.events.chat.message.MessageReceivedEvent;
import com.samczsun.skype4j.events.chat.sent.TypingReceivedEvent;
import pro.zackpollard.telegrambot.api.chat.message.send.ChatAction;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableChatAction;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;

import java.util.List;

/**
 * @author Zack Pollard
 */
public class SkypeEventsListener implements Listener {

    private final SkypeToTelegramBot instance;
    private final TelegramBot telegramBot;
    private final Integer telegramID;

    public SkypeEventsListener(SkypeToTelegramBot instance, TelegramBot telegramBot, Integer telegramID) {

        this.instance = instance;
        this.telegramBot = telegramBot;
        this.telegramID = telegramID;
    }

    @EventHandler
    public void onMessageReceived(MessageReceivedEvent event) {

        System.out.println(event.getChat().getIdentity());

        List<String> chats = instance.getSkypeManager().getTelegramChats(event.getChat());

        if(chats != null) {

            chats.stream().filter(chat -> !event.getMessage().getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())).forEach(chat -> {

                telegramBot.sendMessage(TelegramBot.getChat(chat), SendableTextMessage.builder().message("*" + (event.getMessage().getSender().getDisplayName() != null ? event.getMessage().getSender().getDisplayName() : event.getMessage().getSender().getUsername()) + "*: " + event.getMessage().getContent().asPlaintext()).parseMode(ParseMode.MARKDOWN).build());
            });
        }
    }

    @EventHandler
    public void onTyping(TypingReceivedEvent event) {

        if(event.isTyping()) {

            List<String> chats = instance.getSkypeManager().getTelegramChats(event.getChat());

            if (chats != null) {

                chats.stream().filter(chat -> !event.getSender().getUsername().equals(instance.getSkypeManager().getSkype(telegramID).getUsername())).forEach(chat -> {

                    telegramBot.sendMessage(TelegramBot.getChat(chat), new SendableChatAction(ChatAction.TYPING_TEXT_MESSAGE));
                });
            }
        }
    }
}