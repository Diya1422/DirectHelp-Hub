import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main - entry point. Wires up all objects and starts the HTTP server.
 * Demonstrates: Object creation, Dependency Injection, all OOP concepts working together.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        // Create storage (JSON file)
        Storage storage = new JsonStorage(Config.DATA_FILE);

        // Create services (demonstrates Inheritance - AdminService extends RequestService)
        RequestService requestService = new RequestService(storage);
        AdminService   adminService   = new AdminService(storage);

        // Create HTTP handler (depends on both services)
        ServerHandler handler = new ServerHandler(requestService, adminService);

        // Start the HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(Config.PORT), 0);
        server.createContext("/", handler);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("====================================");
        System.out.println(" DirectHelp Hub Server Started");
        System.out.println("====================================");
        System.out.println(" Site:   http://localhost:" + Config.PORT);
        System.out.println(" Admin:  http://localhost:" + Config.PORT + "/admin");
        System.out.println(" API:    http://localhost:" + Config.PORT + "/api/requests");
        System.out.println(" Data:   " + Config.DATA_FILE);
        System.out.println("====================================");
        System.out.println(" Press Ctrl+C to stop the server");
        System.out.println();
    }
}
