package io.github.realguystuff.ChatServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader buffReader;
    private BufferedWriter buffWriter;
    private User user;
    private List<ClientHandler> clientHandlers;

    public ClientHandler(Socket socket, List<ClientHandler> clientHandlers) {
        try {
            this.socket = socket;
            this.buffWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientHandlers = clientHandlers;

            // Read the username from the client
            String username = buffReader.readLine();
            this.user = new User(username, socket.getInetAddress().getHostAddress());

            broadcastMessage("SERVER: " + username + " has entered the chat");

        } catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        try {
            while (socket.isConnected()) {
                messageFromClient = buffReader.readLine();
                if (messageFromClient == null) {
                    close();
                    break;
                }
                broadcastMessage(user.getUsername() + ": " + messageFromClient);
            }
        } catch (IOException e) {
            close();
        }
    }

    public void sendMessage(String message) {
        try {
            buffWriter.write(message);
            buffWriter.newLine();
            buffWriter.flush();
        } catch (IOException e) {
            close();
        }
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != this) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public void close() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: " + user.getUsername() + " has left the chat");

        try {
            if (buffReader != null) {
                buffReader.close();
            }
            if (buffWriter != null) {
                buffWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
