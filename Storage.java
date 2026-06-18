import java.util.List;

/**
 * Storage - interface for data persistence.
 * Demonstrates: Abstraction and Polymorphism (any storage backend can implement this).
 */
public interface Storage {
    void save(Request request) throws Exception;
    List<Request> getAll() throws Exception;
    boolean delete(String id) throws Exception;
}
