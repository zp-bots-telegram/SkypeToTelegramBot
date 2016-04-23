package pro.zackpollard.telegrambot.skypetotelegrambot.managers;

import com.samczsun.skype4j.Skype;
import com.samczsun.skype4j.SkypeBuilder;
import com.samczsun.skype4j.chat.messages.ChatMessage;
import com.samczsun.skype4j.exceptions.ChatNotFoundException;
import com.samczsun.skype4j.exceptions.ConnectionException;
import com.samczsun.skype4j.exceptions.InvalidCredentialsException;
import com.samczsun.skype4j.exceptions.SkypeException;
import lombok.Getter;
import pro.zackpollard.telegrambot.api.TelegramBot;
import pro.zackpollard.telegrambot.api.chat.Chat;
import pro.zackpollard.telegrambot.api.chat.GroupChat;
import pro.zackpollard.telegrambot.api.chat.message.send.ParseMode;
import pro.zackpollard.telegrambot.api.chat.message.send.SendableTextMessage;
import pro.zackpollard.telegrambot.api.keyboards.ReplyKeyboardHide;
import pro.zackpollard.telegrambot.api.user.User;
import pro.zackpollard.telegrambot.skypetotelegrambot.SkypeToTelegramBot;
import pro.zackpollard.telegrambot.skypetotelegrambot.skype.listeners.SkypeEventsListener;
import pro.zackpollard.telegrambot.skypetotelegrambot.storage.CredentialStore;
import pro.zackpollard.telegrambot.skypetotelegrambot.storage.PermissionsStore;
import pro.zackpollard.telegrambot.skypetotelegrambot.utils.Utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class SkypeManager {

    private transient final TelegramBot telegramBot;
    private transient final SkypeToTelegramBot instance;

    @Getter
    private final PermissionsStore permissionsStore;

    @Getter
    private final CredentialStore credentialStore;

    //Telegram User to Skype Instance
    private transient final Map<Long, Skype> telegramToSkypeLink;

    //Skype Instance to Telegram User
    private transient final Map<Skype, Long> skypeToTelegramLink;

    //Telegram Chat's Telegram User to Skype Chat's
    private final Map<String, TelegramIDSkypeChat> telegramChatToSkypeChat;

    //<TelegramUserID, <SkypeChatID, TelegramChatID>>
    private final Map<Long, Map<String, String>> skypeChatToTelegramChat;

    //<SkypeChatID, LastMessageID>
    @Getter
    private final Map<String, String> lastSyncedSkypeMessage;

    @Getter
    private transient final Map<String, Map<String, String>> linkingQueue;

    public SkypeManager() {

        this.instance = SkypeToTelegramBot.getInstance();
        this.telegramBot = instance.getTelegramBot();

        this.permissionsStore = new PermissionsStore();
        this.credentialStore = new CredentialStore();

        this.telegramToSkypeLink = new HashMap<>();
        this.skypeToTelegramLink = new HashMap<>();
        this.telegramChatToSkypeChat = new HashMap<>();
        this.skypeChatToTelegramChat = new HashMap<>();
        this.linkingQueue = new HashMap<>();
        this.lastSyncedSkypeMessage = new HashMap<>();
    }

    public void postLoadInit() {

        for(Map.Entry<Long, CredentialStore.Credentials> credentials : credentialStore.getTelegramToSkypeCredentials().entrySet()) {

            Chat telegramUser = TelegramBot.getChat(credentials.getKey());

            if(skypeLogin(credentials.getKey(), credentials.getValue().getUsername(), credentials.getValue().getPassword())) {

                telegramUser.sendMessage("The Telegram bot restarted and your link to skype has been re-established successfully.", telegramBot);
            } else {

                telegramUser.sendMessage("Something went wrong when trying to re-establish your link after the bot restarted, please try to authenticate again.", telegramBot);
            }
        }

        for(Map.Entry<String, TelegramIDSkypeChat> entry : new HashMap<>(telegramChatToSkypeChat).entrySet()) {

            try {
                Skype skype = telegramToSkypeLink.get(entry.getValue().getTelegramUser());

                com.samczsun.skype4j.chat.Chat chat = skype.getChat(entry.getValue().getSkypeChat());

                if(chat == null) {

                    chat = telegramToSkypeLink.get(entry.getValue().getTelegramUser()).loadChat(entry.getValue().getSkypeChat());
                }

                chat.loadMoreMessages(50);

                String lastSyncedMessageId = lastSyncedSkypeMessage.get(chat.getIdentity());

                if(lastSyncedMessageId != null) {

                    List<ChatMessage> messagesToSend = new LinkedList<>();

                    for(int i = 0; i < chat.getAllMessages().size(); ++i) {

                        System.out.println(chat.getAllMessages().get(i).getContent().asPlaintext());

                        if(chat.getAllMessages().get(i).getId().equals(lastSyncedMessageId)) {

                            break;
                        }

                        messagesToSend.add(chat.getAllMessages().get(i));
                    }

                    for (int i = 0; i <= messagesToSend.size() - 1; ++i) {

                        ChatMessage message = messagesToSend.get(i);

                        TelegramBot.getChat(entry.getKey()).sendMessage(SendableTextMessage.builder().message("*" + (message.getSender().getDisplayName() != null ? message.getSender().getDisplayName() : message.getSender().getUsername()) + "*: " + Utils.escapeMarkdownText(message.getContent().asPlaintext())).parseMode(ParseMode.MARKDOWN).build(), telegramBot);

                        lastSyncedSkypeMessage.put(chat.getIdentity(), message.getId());

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {

                    if (chat.getAllMessages().size() > 0) {

                        lastSyncedSkypeMessage.put(chat.getIdentity(), chat.getAllMessages().get(0).getId());
                    }
                }

                instance.saveSkypeManager();
            } catch (ConnectionException e) {
                e.printStackTrace();
            } catch (ChatNotFoundException e) {
                telegramChatToSkypeChat.remove(entry.getKey());
                TelegramBot.getChat(entry.getKey()).sendMessage("It seems that this chat no longer exists on skype, if this is incorrect then please re-link this chat.", telegramBot);
            }
        }

        System.out.println("This is the map printing section...");

        for(Map.Entry<Long, Map<String, String>> map : skypeChatToTelegramChat.entrySet()) {

            System.out.println("This is the map for user: " + map.getKey());

            for(Map.Entry<String, String> entry : map.getValue().entrySet()) {

                System.out.println("< " + map.getKey() + " < " + entry.getKey() + ", " + entry.getValue() + " >");
            }
        }
    }

    public boolean addUser(User telegramUser, String username, String password) {

        if(skypeLogin(telegramUser.getId(), username, password)) {

            getCredentialStore().addCredentials(telegramUser, username, password);

            instance.saveSkypeManager();

            return true;
        }

        return false;
    }

    public boolean removeUser(User telegramUser) {

        if(isSetup(telegramUser.getId())) {

            boolean removed = credentialStore.removeCredentials(telegramUser);
            instance.saveSkypeManager();
            return removed;
        }

        return false;
    }

    private boolean skypeLogin(Long telegramUserID, String username, String password) {

        try {

            Skype skype = new SkypeBuilder(username, password).withAllResources().withExceptionHandler((errorSource, throwable, b) -> {
                throwable.printStackTrace();

                telegramBot.sendMessage(TelegramBot.getChat(telegramUserID), SendableTextMessage.builder().message("*An error occurred*\n*Error Source*: " + errorSource.name() + "\n*Message*: " + throwable.getMessage()).parseMode(ParseMode.MARKDOWN).build());

                String stacktrace = "";
                for(StackTraceElement element : throwable.getStackTrace()) {

                    stacktrace += element.toString() + '\n';
                }
                TelegramBot.getChat(telegramUserID).sendMessage(SendableTextMessage.builder().message("The stacktrace is:\n" + stacktrace).build(), telegramBot);
                TelegramBot.getChat(telegramUserID).sendMessage("Please send the previous two messages to @zackpollard on telegram.", telegramBot);
            }).build();

            skype.login();
            skype.getEventDispatcher().registerListener(new SkypeEventsListener(instance, telegramBot, telegramUserID));
            skype.subscribe();

            telegramToSkypeLink.put(telegramUserID, skype);
            skypeToTelegramLink.put(skype, telegramUserID);

            skypeChatToTelegramChat.putIfAbsent(telegramUserID, new HashMap<>());

            return true;
        } catch (InvalidCredentialsException e) {
            telegramBot.sendMessage(TelegramBot.getChat(telegramUserID), SendableTextMessage.builder().message("Skype credentials were incorrect.").build());
        } catch (SkypeException e) {
            e.printStackTrace();
            telegramBot.sendMessage(TelegramBot.getChat(telegramUserID), SendableTextMessage.builder().message("An error occurred with the Skype API, please try again later.").build());
        }

        return false;
    }

    public boolean createLink(long telegramUser, GroupChat telegramChat, com.samczsun.skype4j.chat.Chat skypeChat) {

        Map<String, String> userChats = skypeChatToTelegramChat.get(telegramUser);

        String linkedTelegramChatID = userChats.get(skypeChat.getIdentity());

        if(linkedTelegramChatID != null) {

            telegramChat.sendMessage("A link to this chat already exists for another group, the link will be moved to this chat.", telegramBot);
            if(this.removeLink(TelegramBot.getChat(linkedTelegramChatID), telegramUser)) {

                TelegramBot.getChat(linkedTelegramChatID).sendMessage("The skype link in this chat has been removed due to the link being made in another chat.", telegramBot);
            } else {

                telegramChat.sendMessage("Removal of the link failed.", telegramBot);
                return false;
            }
        }

        telegramChatToSkypeChat.put(telegramChat.getId(), new TelegramIDSkypeChat(telegramUser, skypeChat.getIdentity()));

        ChatMessage lastSyncedMessage = skypeChat.getAllMessages().get(0);
        lastSyncedSkypeMessage.put(skypeChat.getIdentity(), lastSyncedMessage != null ? lastSyncedMessage.getId() : null);
        telegramBot.sendMessage(telegramChat, SendableTextMessage.builder().message("The chats have been linked successfully!").replyMarkup(ReplyKeyboardHide.builder().build()).build());
        userChats.put(skypeChat.getIdentity(), telegramChat.getId());

        instance.saveSkypeManager();

        return true;
    }

    public boolean removeLink(Chat telegramChat, long telegramUser) {

        if(isLinked(telegramChat)) {

            String skypeChatID = telegramChatToSkypeChat.get(telegramChat.getId()).getSkypeChat();

            skypeChatToTelegramChat.get(telegramUser).remove(skypeChatID);
            lastSyncedSkypeMessage.remove(skypeChatID);
            telegramChatToSkypeChat.remove(telegramChat.getId());

            instance.saveSkypeManager();

            return true;
        }

        return false;
    }

    public boolean isLinked(Chat telegramChat) {

        return telegramChatToSkypeChat.containsKey(telegramChat.getId());
    }

    public boolean isSetup(Long telegramUser) {

        return telegramToSkypeLink.containsKey(telegramUser);
    }

    //Telegram Chat to Skype Chat
    public com.samczsun.skype4j.chat.Chat getSkypeChat(Chat telegramChat) {

        TelegramIDSkypeChat data = telegramChatToSkypeChat.get(telegramChat.getId());

        if(data != null) {

            return data.getSkypeInstance().getChat(data.getSkypeChat());
        }

        return null;
    }


    public String getTelegramChat(com.samczsun.skype4j.chat.Chat chat, Long telegramID) {

        return skypeChatToTelegramChat.get(telegramID).get(chat.getIdentity());
    }

    public Skype getSkype(User user) {

        return telegramToSkypeLink.get(user.getId());
    }

    public Skype getSkype(long userID) {

        return telegramToSkypeLink.get(userID);
    }

    private class TelegramIDSkypeChat {

        @Getter
        private final long telegramUser;
        @Getter
        private final String skypeChat;

        public TelegramIDSkypeChat(long telegramUser, String skypeChat) {

            this.telegramUser = telegramUser;
            this.skypeChat = skypeChat;
        }

        public Skype getSkypeInstance() {

            return SkypeToTelegramBot.getInstance().getSkypeManager().getSkype(telegramUser);
        }
    }
}