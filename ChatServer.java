package server;

import common.Message;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 5555;
    private static final Set<ClientHandler> clientList = ConcurrentHashMap.newKeySet();
    private static ServerSocket serverSocket;
    private static boolean isRunning = true;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Multi-Threaded Chat Server Starting");
        System.out.println("========================================");
        System.out.println("[SERVER] Server starting on port " + PORT);
        System.out.println("[SERVER] Waiting for client connections...");
        System.out.println("[SERVER] Press Ctrl+C to stop server");
        System.out.println("========================================\n");

        try {
            serverSocket = new ServerSocket(PORT);

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("[SERVER] New connection: " + clientSocket.getInetAddress().getHostAddress());

                    ClientHandler handler = new ClientHandler(clientSocket);
                    clientList.add(handler);

                    Thread clientThread = new Thread(handler);
                    clientThread.start();

                    System.out.println("[SERVER] Total connected clients: " + clientList.size());

                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("[SERVER] Error accepting connection: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Server error: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public static void broadcastMessage(Message message) {
        System.out.println("[BROADCAST] Sending to " + clientList.size() + " clients");

        for (ClientHandler client : clientList) {
            try {
                client.sendMessage(message);
            } catch (IOException e) {
                System.err.println("[BROADCAST] Failed to send to " + client.getUsername() + ": " + e.getMessage());
            }
        }
    }

    public static void removeClient(ClientHandler client) {
        clientList.remove(client);
        System.out.println("[SERVER] Client removed: " + client.getUsername());
        System.out.println("[SERVER] Total connected clients: " + clientList.size());
    }

    public static void shutdown() {
        isRunning = false;
        System.out.println("\n[SERVER] Shutting down server...");

        for (ClientHandler client : clientList) {
            client.close();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("[SERVER] Server socket closed");
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Error closing server: " + e.getMessage());
        }

        System.out.println("[SERVER] Server shutdown complete");
        System.out.println("========================================");
    }
}