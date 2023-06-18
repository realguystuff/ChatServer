package io.github.realguystuff.ChatServer;

public class User {
    private String username;
    private String IP;

    public User(String username, String IP) {
        this.username = username;
        this.IP = IP;
    }

    public String getUsername() {
        return username;
    }
    
    public String getIP() {
        return IP;
    }
}
