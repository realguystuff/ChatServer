package io.github.realguystuff.ChatServer;

import java.util.List;

public interface ChatPlugin {
    void onEnable();
    void onDisable();
    void incomingMessage(String message);
    void userJoin(User user);
    List<String> getUserList();
    void kick(User user, String reason);

    default void ban(User user) {
        PluginManager.getInstance().banUser(user, "No reason specified");
    }
    default void ban(User user, String reason) {
        PluginManager.getInstance().banUser(user, reason);
    }
}