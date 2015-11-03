package pro.zackpollard.telegrambot.skypetotelegrambot.telegram.listeners;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.chat.Chat;
import com.samczsun.skype4j.chat.GroupChat;
import com.samczsun.skype4j.chat.IndividualChat;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.ChatType;
import pro.zackpollard.telegrambot.api.chat.message.Message;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.event.Listener;
import pro.zackpollard.telegrambot.api.event.chat.message.CommandMessageReceivedEvent;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardMarkup;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class TelegramCommandListener implements Listener {

    private final SkypeToTelegramBot instance;
    private final TelegramBot telegramBot;

    public TelegramCommandListener(SkypeToTelegramBot instance) {

        this.instance = instance;
        this.telegramBot = instance.getTelegramBot();
    }

    @Override
    public void onCommandMessageReceived(CommandMessageReceivedEvent event) {

        switch(event.getCommand().toLowerCase()) {

            case "login": {

                if(event.getChat().getType().equals(ChatType.PRIVATE)) {

                    if(event.getArgs().length == 2) {

                        boolean success = instance.getSkypeManager().addUser(event.getMessage().getSender(), event.getArgs()[0], event.getArgs()[1]);
                        if(success) event.getChat().sendMessage("Successfully authorised with skype.", telegramBot);
                    } else {

                        event.getChat().sendMessage("Correct usage is: /login [username] [password]", telegramBot);
                    }
                }

                break;
            }

            case "link": {

                if(event.getChat().getType().equals(ChatType.GROUP)) {

                    Skype skype = instance.getSkypeManager().getSkype(event.getMessage().getSender());

                    if(skype != null) {

                        Map<String, String> chats = new HashMap<>();

                        for(Chat chat : skype.getAllChats()) {

                            if(chat instanceof GroupChat) {

                                chats.put(((GroupChat) chat).getTopic(), chat.getIdentity());
                            } else if(chat instanceof IndividualChat) {

                                chats.put(chat.getIdentity().substring(2), chat.getIdentity());
                            }
                        }

                        ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder keyboardMarkupBuilder = ReplyKeyboardMarkup.builder().resize(true).oneTime(true).selective(true);

                        chats.keySet().forEach(keyboardMarkupBuilder::addRow);

                        Message message = telegramBot.sendMessage(event.getChat(), SendableTextMessage.builder().message("Please select the chat you want to link.").replyMarkup(keyboardMarkupBuilder.build()).replyTo(event.getMessage()).build());

                        instance.getSkypeManager().getLinkingQueue().put(event.getChat().getId(), chats);
                    }
                }

                break;
            }

            case "logout": {

                if(event.getChat().getType().equals(ChatType.PRIVATE)) {

                    if(instance.getSkypeManager().removeUser(((pro.zackpollard.telegrambot.api.chat.IndividualChat) event.getChat()).getPartner())) {

                        event.getChat().sendMessage("You have been logged out successfully.", telegramBot);
                    } else {

                        event.getChat().sendMessage("You weren't logged in.", telegramBot);
                    }
                } else {

                    event.getChat().sendMessage("This command can only be used in a private chat.", telegramBot);
                }

                break;
            }

            case "unlink": {

                if(event.getChat().getType().equals(ChatType.GROUP)) {

                    if(instance.getSkypeManager().removeLink(event.getChat())) {

                        event.getChat().sendMessage("The link to this chat was successfully removed.", telegramBot);
                    } else {

                        event.getChat().sendMessage("This chat is not linked to a skype chat.", telegramBot);
                    }
                }

                break;
            }
        }
    }
}