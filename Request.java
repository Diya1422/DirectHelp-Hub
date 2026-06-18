/**
 * Request - concrete model class that implements HelpRequest.
 * Demonstrates: Classes, Objects, Encapsulation (private fields + getters/setters).
 */
public class Request implements HelpRequest {

    private String id;
    private String name;
    private String whatsapp;
    private String email;
    private String category;
    private String message;
    private String timestamp;
    private String status;

    // Constructor for creating a new request (server assigns id, timestamp, status)
    public Request(String id, String name, String whatsapp, String email,
                   String category, String message, String timestamp, String status) {
        this.id        = id;
        this.name      = name;
        this.whatsapp  = whatsapp;
        this.email     = email;
        this.category  = category;
        this.message   = message;
        this.timestamp = timestamp;
        this.status    = status;
    }

    // --- Getters (Encapsulation) ---
    @Override public String getId()        { return id; }
    @Override public String getName()      { return name; }
    @Override public String getCategory()  { return category; }
    @Override public String getMessage()   { return message; }
    @Override public String getTimestamp() { return timestamp; }
    public String getWhatsapp()            { return whatsapp; }
    public String getEmail()               { return email; }
    public String getStatus()              { return status; }

    // --- Setters ---
    public void setStatus(String status) { this.status = status; }

    // --- Serialize to JSON string ---
    @Override
    public String toJson() {
        return "{"
            + "\"id\":"        + jsonStr(id)        + ","
            + "\"name\":"      + jsonStr(name)       + ","
            + "\"whatsapp\":"  + jsonStr(whatsapp)   + ","
            + "\"email\":"     + jsonStr(email)      + ","
            + "\"category\":"  + jsonStr(category)   + ","
            + "\"message\":"   + jsonStr(message)    + ","
            + "\"timestamp\":" + jsonStr(timestamp)  + ","
            + "\"status\":"    + jsonStr(status)
            + "}";
    }

    @Override
    public String toString() {
        return "Request{id=" + id + ", name=" + name + ", category=" + category + "}";
    }

    // Wraps a value in JSON string quotes with escape handling
    private String jsonStr(String value) {
        if (value == null) return "\"\"";
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            + "\"";
    }
}
