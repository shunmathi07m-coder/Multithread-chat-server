# Multi-Threaded Java Chat Server

A production-style, scalable client-server chat application built with Core Java and Socket Programming. Multiple clients can connect simultaneously and exchange messages in real-time with full concurrency support.

---

## 📋 Project Overview

This project demonstrates a **multi-threaded chat server** where:
- Multiple clients connect simultaneously
- Each client gets a dedicated handler thread
- Messages are broadcast to all connected clients in real-time
- Join/leave notifications are automatically sent
- Thread-safe operations using `ConcurrentHashSet`
- Graceful handling of client disconnections

**Perfect for:** Learning Java networking, concurrency, thread management, and software architecture principles.

---

## ✨ Features

### Server Features
- ✅ Java Socket Programming (TCP)
- ✅ Accepts multiple simultaneous client connections
- ✅ Dedicated thread for each client (Thread-Per-Client model)
- ✅ Centralized thread-safe client list (`ConcurrentHashSet`)
- ✅ Real-time message broadcasting to all clients
- ✅ Join/leave notifications for all clients
- ✅ Graceful handling of unexpected disconnections
- ✅ Unique username support (optional enhancement)
- ✅ Server-side logging with timestamps
- ✅ Thread-safe access to shared resources
- ✅ Clean shutdown mechanism

### Client Features
- ✅ Connect using IP address and port number
- ✅ Username authentication
- ✅ Send and receive messages concurrently
- ✅ Real-time message display
- ✅ Clean exit functionality (type `exit`)
- ✅ Connection status messages
- ✅ Automatic reconnection support (future)

---

## 🛠️ Technologies Used

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Java | 17+ |
| Networking | Java Socket API | Built-in |
| Concurrency | Java Threads | `java.lang.Thread` |
| Thread Safety | Concurrent Collections | `ConcurrentHashMap.newKeySet()` |
| I/O | Java NIO (partial) | `BufferedReader`, `PrintWriter` |
| Build Tool | Manual compilation | `javac` |
| IDE | VS Code | Extension Pack for Java |

---

## 🏗️ Concurrency Design

### Thread Architecture
- **Main Server Thread**: Accepts client connections
- **Client Handler Threads**: One per client (dedicated)
- **Client Receiver Threads**: One per client (for receiving messages)

### Concurrency Techniques
- **Thread-Per-Client Model**: Each client gets dedicated thread
- **Thread-Safe Collections**: `ConcurrentHashSet` for client list
- **Blocking I/O**: Simple, intuitive socket communication
- **No Explicit Locks**: Avoids deadlock by using concurrent collections

### Key Concurrency Concepts Demonstrated
- Thread creation and lifecycle management
- Thread synchronization and thread safety
- Race condition prevention
- Deadlock avoidance
- Concurrent message broadcasting
- Graceful thread termination

---

## 📁 Project Structure
