package client;

import common.Message;
import java.io.*;
import java.net.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;
    private boolean isConnected = false;
    private Thread receiverThread;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Multi-Threaded Chat Client");
        System.out.println("========================================");

        ChatClient client = new ChatClient();
        client.start();
    }

    public void start() {
        String host = "localhost";
        int port = 5555;

        System.out.print("[CLIENT] Enter server IP (default: localhost): ");
        host = readLine();
        if (host == null || host.trim().isEmpty()) {
            host = "localhost";
        }

        System.out.print("[CLIENT] Enter port (default: 5555): ");
        String portStr = readLine();
        if (portStr != null && !portStr.trim().isEmpty()) {
            port = Integer.parseInt(portStr.trim());
        }

        System.out.print("[CLIENT] Enter your username: ");
        username = readLine();

        if (username == null || username.trim().isEmpty()) {
            System.out.println("[ERROR] Username is required!");
            return;
        }

        username = username.trim();
        connect(host, port);

        if (!isConnected) {
            System.out.println("[ERROR] Failed to connect to server");
            return;
        }

        startReceiver();
        messageLoop();
    }

    private void connect(String host, int port) {
        System.out.println("[CLIENT] Connecting to " + host + ":" + port);

        try {
            socket = new Socket(host, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            output.println(username);

            isConnected = true;
            System.out.println("[CLIENT] Connected successfully!");
            System.out.println("[CLIENT] Type messages to send. Use 'exit' to disconnect.");
            System.out.println("========================================\n");

        } catch (IOException e) {
            System.err.println("[ERROR] Connection failed: " + e.getMessage());
            isConnected = false;
        }
    }

    private void startReceiver() {
        receiverThread = new Thread(() -> {
            System.out.println("[THREAD] Receiver thread started");

            try {
                while (isConnected) {
                    String messageData = input.readLine();

                    if (messageData == null) {
                        break;
                    }

                    Message message = Message.fromString(messageData);
                    if (message != null) {
                        System.out.println(message.formattedString());
                    }
                }
            } catch (IOException e) {
                if (isConnected) {
                    System.err.println("[ERROR] Receiver error: " + e.getMessage());
                }
            } finally {
                isConnected = false;
                System.out.println("[THREAD] Receiver thread terminated");
            }
        });

        receiverThread.start();
    }

    private void messageLoop() {
        try {
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));

            while (isConnected) {
                System.out.print("> ");
                String message = consoleInput.readLine();

                if (message == null) {
                    break;
                }

                message = message.trim();

                if (message.equals("exit")) {
                    disconnect();
                    break;
                }

                if (!message.isEmpty()) {
                    Message msg = new Message(username, message, "CHAT");
                    output.println(msg.toString());
                }
            }

        } catch (IOException e) {
            System.err.println("[ERROR] Message loop error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void disconnect() {
        isConnected = false;

        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("\n[CLIENT] Disconnected from server");
            System.out.println("========================================");

        } catch (IOException e) {
            System.err.println("[ERROR] Disconnect error: " + e.getMessage());
        }
    }

    private String readLine() {
        try {
            BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
            return consoleInput.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}