import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminService - extends RequestService with admin-only operations.
 * Demonstrates: Inheritance (extends RequestService), Polymorphism (overrides search).
 */
public class AdminService extends RequestService {

    public AdminService(Storage storage) {
        super(storage); // calls RequestService constructor (Inheritance)
    }

    // Delete a request by id
    public boolean delete(String id) throws Exception {
        boolean removed = storage.delete(id);
        if (removed) System.out.println("[AdminService] Deleted request: " + id);
        return removed;
    }

    // Search and filter requests - overrides base concept of getAll with filtering
    public List<Request> search(String query, String category) throws Exception {
        List<Request> all = storage.getAll();
        return all.stream()
            .filter(r -> {
                boolean matchCat = category == null || category.isEmpty()
                    || r.getCategory().equalsIgnoreCase(category);
                boolean matchQ = query == null || query.isEmpty()
                    || r.getName().toLowerCase().contains(query.toLowerCase())
                    || r.getMessage().toLowerCase().contains(query.toLowerCase())
                    || r.getCategory().toLowerCase().contains(query.toLowerCase());
                return matchCat && matchQ;
            })
            .collect(Collectors.toList());
    }

    // Export all requests as a JSON array string
    public String export() throws Exception {
        List<Request> all = storage.getAll();
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < all.size(); i++) {
            sb.append("  ").append(all.get(i).toJson());
            if (i < all.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    // Summary stats
    public Map<String, Object> stats() throws Exception {
        List<Request> all = storage.getAll();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", all.size());
        map.put("pending", all.stream().filter(r -> "pending".equals(r.getStatus())).count());
        return map;
    }
}
