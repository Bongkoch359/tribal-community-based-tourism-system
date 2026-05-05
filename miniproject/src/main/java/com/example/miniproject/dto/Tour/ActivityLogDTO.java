package com.example.miniproject.dto.Tour;

public class ActivityLogDTO {
    
    private String message;
    private String type;   // green, amber, red, gray
    private String timestamp;
 
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
 
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
 
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
