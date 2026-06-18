import com.sun.net.httpserver.*;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

/**
 * ServerHandler - HTTP request router.
 * Routes API calls to services and serves static frontend files.
 */
public class ServerHandler implements HttpHandler {

    private final RequestService requestService;
    private final AdminService   adminService;

    public ServerHandler(RequestService requestService, AdminService adminService) {
        this.requestService = requestService;
        this.adminService   = adminService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path   = exchange.getRequestURI().getPath();

        // CORS headers for browser requests
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            if (path.startsWith("/api/")) {
                handleApi(exchange, method, path);
            } else {
                serveStatic(exchange, path);
            }
        } catch (Exception e) {
            System.err.println("[Error] " + path + ": " + e.getMessage());
            sendJson(exchange, 500, "{\"success\":false,\"message\":\"Internal server error\"}");
        }
    }

    // --- API routing ---
    private void handleApi(HttpExchange exchange, String method, String path) throws Exception {
        // POST /api/requests - create a new request
        if ("POST".equalsIgnoreCase(method) && path.equals("/api/requests")) {
            String body = readBody(exchange);
            String name     = extractJson(body, "name");
            String whatsapp = extractJson(body, "whatsapp");
            String email    = extractJson(body, "email");
            String category = extractJson(body, "category");
            String message  = extractJson(body, "message");

            if (name.isEmpty() || whatsapp.isEmpty() || category.isEmpty() || message.isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"message\":\"name, whatsapp, category, and message are required\"}");
                return;
            }

            Request r = requestService.submit(name, whatsapp, email, category, message);
            sendJson(exchange, 201, "{\"success\":true,\"message\":\"Request saved\",\"id\":\"" + r.getId() + "\"}");
            return;
        }

        // GET /api/requests - get all (with optional search/category params)
        if ("GET".equalsIgnoreCase(method) && path.equals("/api/requests")) {
            String query    = getQueryParam(exchange.getRequestURI(), "search");
            String category = getQueryParam(exchange.getRequestURI(), "category");
            List<Request> results = adminService.search(query, category);
            StringBuilder sb = new StringBuilder("{\"success\":true,\"count\":" + results.size() + ",\"requests\":[");
            for (int i = 0; i < results.size(); i++) {
                sb.append(results.get(i).toJson());
                if (i < results.size() - 1) sb.append(",");
            }
            sb.append("]}");
            sendJson(exchange, 200, sb.toString());
            return;
        }

        // DELETE /api/requests/{id} - delete a request
        if ("DELETE".equalsIgnoreCase(method) && path.startsWith("/api/requests/")) {
            String id = path.substring("/api/requests/".length());
            boolean deleted = adminService.delete(id);
            if (deleted) {
                sendJson(exchange, 200, "{\"success\":true,\"message\":\"Deleted\"}");
            } else {
                sendJson(exchange, 404, "{\"success\":false,\"message\":\"Request not found\"}");
            }
            return;
        }

        // GET /api/export - export all as JSON
        if ("GET".equalsIgnoreCase(method) && path.equals("/api/export")) {
            String json = adminService.export();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"requests-export.json\"");
            byte[] bytes = json.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
            return;
        }

        sendJson(exchange, 404, "{\"success\":false,\"message\":\"API endpoint not found\"}");
    }

    // --- Static file serving ---
    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        if (path.equals("/") || path.isEmpty()) path = "/index.html";
        if (path.equals("/admin"))              path = "/admin.html";

        File file = new File(Config.FRONTEND_DIR + path);
        if (!file.exists() || !file.isFile()) {
            String body = "<h1>404 - Not Found</h1>";
            exchange.getResponseHeaders().add("Content-Type", "text/html");
            exchange.sendResponseHeaders(404, body.length());
            exchange.getResponseBody().write(body.getBytes());
            exchange.getResponseBody().close();
            return;
        }

        String mime = mimeType(path);
        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    // --- Helpers ---
    private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes());
    }

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki == -1) return "";
        int ci = json.indexOf(":", ki + search.length());
        if (ci == -1) return "";
        int vs = ci + 1;
        while (vs < json.length() && Character.isWhitespace(json.charAt(vs))) vs++;
        if (vs >= json.length() || json.charAt(vs) != '"') return "";
        vs++;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        while (vs < json.length()) {
            char c = json.charAt(vs++);
            if (esc) { sb.append(c == 'n' ? '\n' : c == 't' ? '\t' : c); esc = false; }
            else if (c == '\\') esc = true;
            else if (c == '"') break;
            else sb.append(c);
        }
        return sb.toString().trim();
    }

    private String getQueryParam(URI uri, String param) {
        String q = uri.getQuery();
        if (q == null) return "";
        for (String part : q.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(param)) return kv[1];
        }
        return "";
    }

    private String mimeType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".ico"))  return "image/x-icon";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream";
    }
}
