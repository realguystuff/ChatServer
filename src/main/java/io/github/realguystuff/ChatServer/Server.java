package io.github.realguystuff.ChatServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.clientHandlers = new ArrayList<>();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New Friend Connected");

                ClientHandler clientHandler = new ClientHandler(socket, clientHandlers);
                Thread thread = new Thread(clientHandler);
                thread.start();

                clientHandlers.add(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
