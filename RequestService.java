import java.time.LocalDateTime;
import java.util.*;

/**
 * RequestService - handles the user-facing business logic.
 * Demonstrates: Classes, Objects, Encapsulation.
 */
public class RequestService {

    protected Storage storage;

    public RequestService(Storage storage) {
        this.storage = storage;
    }

    // Submit a new help request
    public Request submit(String name, String whatsapp, String email,
                          String category, String message) throws Exception {
        String id        = java.util.UUID.randomUUID().toString();
        String timestamp = LocalDateTime.now().toString();
        Request r = new Request(id, name, whatsapp, email, category, message, timestamp, "pending");
        storage.save(r);
        System.out.println("[RequestService] Saved: " + r);
        return r;
    }

    // Get all requests
    public List<Request> getAll() throws Exception {
        return storage.getAll();
    }
}
