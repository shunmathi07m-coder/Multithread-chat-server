package common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message class represents a chat message with sender, content, and timestamp.
 * Serializable for network transmission.
 */
public class Message {
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    private String type; // "CHAT", "JOIN", "LEAVE"

    public Message(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String formattedString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String timeStr = timestamp.format(formatter);
        return "[" + timeStr + "] " + sender + ": " + content;
    }

    @Override
    public String toString() {
        return sender + "|" + content + "|" + type + "|" + timestamp;
    }

    public static Message fromString(String data) {
        String[] parts = data.split("|");
        if (parts.length >= 4) {
            return new Message(parts[0], parts[1], parts[2]);
        }
        return null;
    }
}