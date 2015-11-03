package pro.zackpollard.telegrambot.skypetotelegrambot.storage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pro.zackpollard.telegrambot.api.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class CredentialStore {

    @Getter
    private final Map<Integer, Credentials> telegramToSkypeCredentials;

    public CredentialStore() {

        this.telegramToSkypeCredentials = new HashMap<>();
    }

    public boolean addCredentials(User telegramUser, String username, String password) {

        if(!telegramToSkypeCredentials.containsKey(telegramUser.getId())) {

            telegramToSkypeCredentials.put(telegramUser.getId(), new Credentials(username, password));
            return true;
        }

        return false;
    }

    public boolean removeCredentials(User telegramUser) {

        return telegramToSkypeCredentials.remove(telegramUser.getId()) != null;
    }

    public Credentials getCredentials(User user) {

        return telegramToSkypeCredentials.get(user.getId());
    }

    @RequiredArgsConstructor
    public class Credentials {

        @Getter
        private final String username;
        @Getter
        private final String password;
    }
}