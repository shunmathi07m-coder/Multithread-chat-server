# Concurrency Model Documentation

## Multi-Threaded Chat Server - Thread Architecture

This document explains the concurrency model, thread lifecycle, synchronization strategy, and scalability considerations of the Multi-Threaded Chat Server.

---

## Thread Architecture

### Overview

The chat server uses a **Thread-Per-Client** concurrency model where:
- One main server thread accepts client connections
- Each client gets a dedicated handler thread
- Messages are broadcast to all connected clients thread-safely

```mermaid
graph TD
    subgraph "Server Side"
        Main[Main Server Thread<br/>Accepts Connections]
        Handler1[Client Handler Thread 1<br/>Alice]
        Handler2[Client Handler Thread 2<br/>Bob]
        Handler3[Client Handler Thread 3<br/>Charlie]
        ClientList[Thread-Safe Client List<br/>ConcurrentHashSet]
    end
    
    subgraph "Client Side"
        ClientA[Client A Terminal<br/>Alice]
        ClientB[Client B Terminal<br/>Bob]
        ClientC[Client C Terminal<br/>Charlie]
        ReceiverA[Receiver Thread A]
        ReceiverB[Receiver Thread B]
        ReceiverC[Receiver Thread C]
    end
    
    Main -->|accept()| Handler1
    Main -->|accept()| Handler2
    Main -->|accept()| Handler3
    
    Handler1 -->|add to| ClientList
    Handler2 -->|add to| ClientList
    Handler3 -->|add to| ClientList
    
    Handler1 <-->|Socket I/O| ClientA
    Handler2 <-->|Socket I/O| ClientB
    Handler3 <-->|Socket I/O| ClientC
    
    ReceiverA -->|read messages| ClientA
    ReceiverB -->|read messages| ClientB
    ReceiverC -->|read messages| ClientC
    
    ClientList -->|broadcast| Handler1
    ClientList -->|broadcast| Handler2
    ClientList -->|broadcast| Handler3
```

---

## Thread Types

### 1. Main Server Thread

**Purpose:** Accept incoming client connections

**Code Location:** `ChatServer.java - main()`

**Lifecycle:**
```java
public static void main(String[] args) {
    ServerSocket serverSocket = new ServerSocket(PORT);  // Create socket
    
    while (isRunning) {
        Socket clientSocket = serverSocket.accept();     // BLOCK: Wait for client
        ClientHandler handler = new ClientHandler(clientSocket);
        Thread clientThread = new Thread(handler);       // Create new thread
        clientThread.start();                            // START thread
    }
}
```

**State:** 
- **RUNNING**: Active in accept loop
- **BLOCKING**: Waiting on `serverSocket.accept()`
- **TERMINATED**: When server shuts down

---

### 2. Connection Acceptance Thread

**Note:** In this implementation, the main thread serves as the acceptance thread.

**Alternative Design (for high-load systems):**
```java
// Dedicated acceptance thread (not used in current implementation)
Thread acceptThread = new Thread(() -> {
    while (isRunning) {
        try {
            Socket socket = serverSocket.accept();
            handlerPool.submit(new ClientHandler(socket));
        } catch (IOException e) {
            break;
        }
    }
});
acceptThread.start();
```

---

### 3. Client Handler Threads

**Purpose:** Handle all communication with a single connected client

**Code Location:** `ClientHandler.java - run()`

**Created:** When a new client connects to the server

**Lifecycle:**
```java
public class ClientHandler implements Runnable {
    @Override
    public void run() {
        // STATE: RUNNING
        System.out.println("[THREAD] Client handler started");
        
        // BLOCK: Wait for username
        username = input.readLine();
        
        // BLOCK: Wait for messages in loop
        while (isConnected) {
            String messageData = input.readLine();  // BLOCK
            if (messageData == null) break;
            
            Message message = Message.fromString(messageData);
            ChatServer.broadcastMessage(message);   // Broadcast to all
        }
        
        // STATE: TERMINATED
        handleDisconnect();
        System.out.println("[THREAD] Client handler terminated");
    }
}
```

**States:**
- **NEW**: Thread object created, not started yet
- **RUNNABLE**: `start()` called, waiting for CPU
- **RUNNING**: Actively executing `run()` method
- **BLOCKING**: Waiting on `input.readLine()` for client data
- **TERMINATED**: Client disconnected, `run()` completed

---

### 4. Message Broadcasting Mechanism

**Purpose:** Send messages from one client to all connected clients

**Implementation:**
```java
public static void broadcastMessage(Message message) {
    System.out.println("[BROADCAST] Sending to " + clientList.size() + " clients");
    
    // Thread-safe iteration (ConcurrentHashSet handles synchronization)
    for (ClientHandler client : clientList) {
        try {
            client.sendMessage(message);  // Send to each client
        } catch (IOException e) {
            // Client disconnected during broadcast - handle gracefully
            System.err.println("[BROADCAST] Failed to send to " + 
                client.getUsername());
        }
    }
}
```

**Concurrency Properties:**
- ✅ No explicit locking needed (`ConcurrentHashSet` is thread-safe)
- ✅ Multiple handlers can broadcast simultaneously
- ✅ Each send operation is independent
- ✅ Handles client disconnections during broadcast

---

## Thread Lifecycle

### Thread Creation

**When:** New client connects to server

**Process:**
```java
// Step 1: Accept connection
Socket clientSocket = serverSocket.accept();

// Step 2: Create handler object
ClientHandler handler = new ClientHandler(clientSocket);

// Step 3: Create thread object
Thread clientThread = new Thread(handler);

// Step 4: Start thread (moves to RUNNABLE state)
clientThread.start();

// Step 5: Add to client list (thread-safe)
clientList.add(handler);
```

**State Transition Diagram:**