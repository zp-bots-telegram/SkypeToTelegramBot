package pro.zackpollard.telegrambot.skypetotelegrambot.storage;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Zack Pollard
 */
public class PermissionsStore {

    @Getter
    private final Map<Integer, UserRole> userRoles;

    public PermissionsStore() {

        this.userRoles = new HashMap<>();
    }

    public UserRole getRole(int user) {

        UserRole role = userRoles.get(user);

        if(role == null) role = UserRole.USER;

        return role;
    }

    public void setRole(int user, UserRole userRole) {

        userRoles.put(user, userRole);
    }

    public enum UserRole {

        USER,
        SUPERUSER
    }
}