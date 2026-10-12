package server;

import common.Message;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String username;
    private BufferedReader input;
    private PrintWriter output;
    private boolean isConnected = true;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to initialize client: " + e.getMessage());
            close();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void run() {
        System.out.println("[THREAD] Client handler started for: " + clientSocket.getInetAddress());

        try {
            username = input.readLine();

            if (username == null || username.trim().isEmpty()) {
                System.err.println("[ERROR] Invalid username received");
                close();
                return;
            }

            username = username.trim();
            System.out.println("[SERVER] Client connected with username: " + username);

            Message welcome = new Message("SERVER", "Welcome to the chat! " + username + ", you are now connected.", "CHAT");
            sendMessage(welcome);

            Message joinMsg = new Message("SERVER", username + " has joined the chat!", "JOIN");
            ChatServer.broadcastMessage(joinMsg);

            while (isConnected) {
                String messageData = input.readLine();

                if (messageData == null) {
                    break;
                }

                Message message = Message.fromString(messageData);
                if (message != null) {
                    System.out.println("[MESSAGE] " + username + ": " + message.getContent());
                    ChatServer.broadcastMessage(message);
                }
            }

        } catch (IOException e) {
            if (isConnected) {
                System.err.println("[ERROR] Connection lost for " + username + ": " + e.getMessage());
            }
        } finally {
            handleDisconnect();
        }

        System.out.println("[THREAD] Client handler thread terminated for: " + username);
    }

    public void sendMessage(Message message) throws IOException {
        if (isConnected && output != null) {
            output.println(message.toString());
        }
    }

    private void handleDisconnect() {
        isConnected = false;
        System.out.println("[SERVER] " + username + " disconnected");

        Message leaveMsg = new Message("SERVER", username + " has left the chat!", "LEAVE");
        ChatServer.broadcastMessage(leaveMsg);

        ChatServer.removeClient(this);
        close();
    }

    public void close() {
        isConnected = false;

        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
            System.out.println("[THREAD] Client connection closed: " + username);
        } catch (IOException e) {
            System.err.println("[ERROR] Error closing connection: " + e.getMessage());
        }
    }
}