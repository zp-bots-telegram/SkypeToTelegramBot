package pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners;

import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.exceptions.SkypeException;
import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.GroupChat;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.TextMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardHide;

import java.io.IOException;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class TelegramEventsListener implements Listener {

    private final SkypeToTelegramBot instance;
    private final TelegramBot telegramBot;

    public TelegramEventsListener(SkypeToTelegramBot instance) {

        this.instance = instance;
        this.telegramBot = instance.getTelegramBot();
    }

    @Override
    public void onTextMessageReceived(TextMessageReceivedEvent event) {

        if(event.getChat().getType().equals(ChatType.GROUP)) {

            Chat chat = instance.getSkypeManager().getSkypeChat(event.getChat());

            if(chat != null) {

                try {
                    chat.sendMessage(Message.create().with(Text.plain(event.getContent().getContent())));
                } catch (SkypeException | IOException e) {
                    e.printStackTrace();
                }
            } else {

                Map<String, String> chats = instance.getSkypeManager().getLinkingQueue().get(event.getChat().getId());

                if (chats != null) {

                    String chatID = chats.get(event.getContent().getContent());

                    if (chatID != null) {

                        instance.getSkypeManager().createLink(event.getMessage().getSender().getId(), (GroupChat) event.getChat(), instance.getSkypeManager().getSkype(event.getMessage().getSender()).getChat(chatID));
                        telegramBot.sendMessage(event.getChat(), SendableTextMessage.builder().message("The chats have been linked successfully!").replyMarkup(new ReplyKeyboardHide()).build());
                    }
                } else {

                    event.getChat().sendMessage("This chat is not linked to a skype chat, use /link to link it!", telegramBot);
                }
            }
        }
    }
}