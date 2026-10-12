
---

### Running State

**When Thread is Active:**
```java
public void run() {
    // RUNNING: Processing username
    username = input.readLine();
    
    // RUNNING: Creating message object
    Message message = new Message(username, content, "CHAT");
    
    // RUNNING: Broadcasting to all clients
    ChatServer.broadcastMessage(message);
}
```

**Characteristics:**
- Thread has CPU time
- Executing Java bytecode
-Performing computations or I/O operations

---

### Waiting/Blocking State

**When Thread Waits:**

**1. Blocking on Network I/O:**
```java
// BLOCKING: Waits until client sends data
String messageData = input.readLine();

// BLOCKING: Waits until server accepts connection
Socket clientSocket = serverSocket.accept();
```

**2. Waiting State (not used in current implementation):**
```java
// Would be WAITING if we used wait()/notify()
synchronized (someObject) {
    someObject.wait();  // Releases lock, waits for notify()
}
```

**State Transition:**