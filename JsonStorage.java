import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * JsonStorage - implements Storage using a JSON flat file.
 * Demonstrates: File Handling, Encapsulation, Interface Implementation.
 * No external libraries - JSON is parsed and generated manually.
 */
public class JsonStorage implements Storage {

    private final String filePath;

    public JsonStorage(String filePath) throws Exception {
        this.filePath = filePath;
        // Create the file with an empty array if it does not exist
        File f = new File(filePath);
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            writeFile("[]");
        }
    }

    // --- Save a new request (append to array) ---
    @Override
    public synchronized void save(Request request) throws Exception {
        List<Request> requests = getAll();
        requests.add(request);
        writeAll(requests);
    }

    // --- Load all requests from file ---
    @Override
    public synchronized List<Request> getAll() throws Exception {
        String content = readFile().trim();
        if (content.isEmpty() || content.equals("[]")) return new ArrayList<>();
        return parseArray(content);
    }

    // --- Delete a request by id ---
    @Override
    public synchronized boolean delete(String id) throws Exception {
        List<Request> requests = getAll();
        int before = requests.size();
        requests.removeIf(r -> r.getId().equals(id));
        if (requests.size() < before) {
            writeAll(requests);
            return true;
        }
        return false;
    }

    // --- Serialize the list back to file ---
    private void writeAll(List<Request> requests) throws Exception {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < requests.size(); i++) {
            sb.append("  ").append(requests.get(i).toJson());
            if (i < requests.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        writeFile(sb.toString());
    }

    // --- Parse a JSON array string into a list of Request objects ---
    private List<Request> parseArray(String json) {
        List<Request> list = new ArrayList<>();
        List<String> objects = splitObjects(json);
        for (String obj : objects) {
            try {
                Request r = parseObject(obj);
                if (r != null) list.add(r);
            } catch (Exception e) {
                System.err.println("Skipping malformed record: " + e.getMessage());
            }
        }
        return list;
    }

    // --- Split a JSON array string into individual object strings ---
    private List<String> splitObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        char prev = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && prev != '\\') inString = !inString;
            if (!inString) {
                if (c == '{') { if (depth++ == 0) start = i; }
                else if (c == '}') { if (--depth == 0 && start != -1) { objects.add(json.substring(start, i + 1)); start = -1; } }
            }
            prev = c;
        }
        return objects;
    }

    // --- Parse a single JSON object string into a Request ---
    private Request parseObject(String json) {
        return new Request(
            extract(json, "id"),
            extract(json, "name"),
            extract(json, "whatsapp"),
            extract(json, "email"),
            extract(json, "category"),
            extract(json, "message"),
            extract(json, "timestamp"),
            extract(json, "status")
        );
    }

    // --- Extract the value of a string field from a flat JSON object ---
    private String extract(String json, String key) {
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki == -1) return "";
        int ci = json.indexOf(":", ki + search.length());
        if (ci == -1) return "";
        int vs = ci + 1;
        while (vs < json.length() && Character.isWhitespace(json.charAt(vs))) vs++;
        if (vs >= json.length() || json.charAt(vs) != '"') return "";
        vs++; // skip opening quote
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        while (vs < json.length()) {
            char c = json.charAt(vs++);
            if (escape) {
                switch (c) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    default:   sb.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // --- File I/O helpers ---
    private String readFile() throws Exception {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private void writeFile(String content) throws Exception {
        Files.write(Paths.get(filePath), content.getBytes());
    }
}
