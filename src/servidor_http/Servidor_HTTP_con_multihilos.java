package servidor_http;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor HTTP multihilo con soporte para archivos estáticos,
 * routing básico, y manejo de errores.
 * 
 * @author Usuario
 * @version 2.0
 */
public class Servidor_HTTP_4 {
    
    private static final Logger LOGGER = Logger.getLogger(Servidor_http_3.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final String PUBLIC_DIR = "public";
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    
    static {
        // Tipos MIME comunes
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("svg", "image/svg+xml");
        MIME_TYPES.put("ico", "image/x-icon");
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("zip", "application/zip");
    }

    public static void main(String[] args) throws IOException {
        int port = PORT;
        
        // Permitir especificar puerto por argumentos
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                LOGGER.warning("Puerto inválido, usando puerto por defecto: " + PORT);
            }
        }
        
        // Crear directorio público si no existe
        Path publicPath = Paths.get(PUBLIC_DIR);
        if (!Files.exists(publicPath)) {
            Files.createDirectories(publicPath);
            crearArchivoIndex(publicPath);
        }
        
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        try (ServerSocket server = new ServerSocket(port)) {
            LOGGER.info("========================================");
            LOGGER.info("Servidor HTTP iniciado correctamente");
            LOGGER.info("Puerto: " + port);
            LOGGER.info("Directorio público: " + publicPath.toAbsolutePath());
            LOGGER.info("Pool de hilos: " + THREAD_POOL_SIZE);
            LOGGER.info("========================================");
            
            while (true) {
                try {
                    Socket socket = server.accept();
                    threadPool.execute(new ClienteHandler(socket));
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error aceptando conexión", e);
                }
            }
        } finally {
            threadPool.shutdown();
        }
    }
    
    /**
     * Crea un archivo index.html de ejemplo
     */
    private static void crearArchivoIndex(Path publicPath) throws IOException {
        String html = "<!DOCTYPE html>\n" +
                "<html lang='es'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <title>Servidor HTTP Java</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }\n" +
                "        h1 { color: #333; }\n" +
                "        .info { background: #f0f0f0; padding: 15px; border-radius: 5px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>¡Bienvenido al Servidor HTTP Java!</h1>\n" +
                "    <div class='info'>\n" +
                "        <p>Este servidor está funcionando correctamente.</p>\n" +
                "        <p>Hora del servidor: <strong>" + new Date() + "</strong></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
        
        Files.writeString(publicPath.resolve("index.html"), html);
    }
    
    /**
     * Manejador de clientes en hilos separados
     */
    static class ClienteHandler implements Runnable {
        private final Socket socket;
        
        public ClienteHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 OutputStream out = socket.getOutputStream()) {
                
                // Leer la petición HTTP
                HttpRequest request = parseRequest(in);
                
                if (request != null) {
                    LOGGER.info(String.format("%s %s - %s", 
                        request.method, 
                        request.path, 
                        socket.getInetAddress().getHostAddress()));
                    
                    // Procesar la petición
                    HttpResponse response = procesarPeticion(request);
                    
                    // Enviar respuesta
                    enviarRespuesta(out, response);
                } else {
                    enviarError(out, 400, "Bad Request");
                }
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error procesando cliente", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Error cerrando socket", e);
                }
            }
        }
        
        /**
         * Parsea la petición HTTP
         */
        private HttpRequest parseRequest(BufferedReader in) throws IOException {
            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return null;
            }
            
            String[] parts = requestLine.split(" ");
            if (parts.length < 3) {
                return null;
            }
            
            HttpRequest request = new HttpRequest();
            request.method = parts[0];
            request.path = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            request.version = parts[2];
            
            // Leer headers
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String key = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    request.headers.put(key.toLowerCase(), value);
                }
            }
            
            return request;
        }
        
        /**
         * Procesa la petición y genera una respuesta
         */
        private HttpResponse procesarPeticion(HttpRequest request) {
            // Rutas especiales
            if ("/api/time".equals(request.path)) {
                return crearRespuestaJSON(200, 
                    String.format("{\"time\": \"%s\", \"timestamp\": %d}", 
                    new Date(), System.currentTimeMillis()));
            }
            
            if ("/api/info".equals(request.path)) {
                return crearRespuestaJSON(200, 
                    String.format("{\"server\": \"Java HTTP Server\", " +
                    "\"version\": \"2.0\", " +
                    "\"java_version\": \"%s\"}", 
                    System.getProperty("java.version")));
            }
            
            // Servir archivos estáticos
            return servirArchivoEstatico(request.path);
        }
        
        /**
         * Sirve un archivo estático del directorio público
         */
        private HttpResponse servirArchivoEstatico(String path) {
            try {
                // Ruta por defecto
                if ("/".equals(path)) {
                    path = "/index.html";
                }
                
                // Prevenir directory traversal
                path = path.replaceAll("\\.\\.", "");
                
                Path filePath = Paths.get(PUBLIC_DIR, path);
                
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    return crearRespuesta404();
                }
                
                byte[] content = Files.readAllBytes(filePath);
                String extension = getExtension(filePath.toString());
                String mimeType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");
                
                HttpResponse response = new HttpResponse();
                response.statusCode = 200;
                response.statusMessage = "OK";
                response.headers.put("Content-Type", mimeType);
                response.headers.put("Content-Length", String.valueOf(content.length));
                response.body = content;
                
                return response;
                
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error leyendo archivo: " + path, e);
                return crearRespuesta500();
            }
        }
        
        /**
         * Crea una respuesta JSON
         */
        private HttpResponse crearRespuestaJSON(int statusCode, String json) {
            HttpResponse response = new HttpResponse();
            response.statusCode = statusCode;
            response.statusMessage = "OK";
            response.headers.put("Content-Type", "application/json; charset=UTF-8");
            response.body = json.getBytes(StandardCharsets.UTF_8);
            response.headers.put("Content-Length", String.valueOf(response.body.length));
            return response;
        }
        
        /**
         * Crea una respuesta 404
         */
        private HttpResponse crearRespuesta404() {
            String html = "<!DOCTYPE html><html><head><title>404 Not Found</title></head>" +
                         "<body><h1>404 - Página no encontrada</h1></body></html>";
            HttpResponse response = new HttpResponse();
            response.statusCode = 404;
            response.statusMessage = "Not Found";
            response.headers.put("Content-Type", "text/html; charset=UTF-8");
            response.body = html.getBytes(StandardCharsets.UTF_8);
            response.headers.put("Content-Length", String.valueOf(response.body.length));
            return response;
        }
        
        /**
         * Crea una respuesta 500
         */
        private HttpResponse crearRespuesta500() {
            String html = "<!DOCTYPE html><html><head><title>500 Internal Server Error</title></head>" +
                         "<body><h1>500 - Error interno del servidor</h1></body></html>";
            HttpResponse response = new HttpResponse();
            response.statusCode = 500;
            response.statusMessage = "Internal Server Error";
            response.headers.put("Content-Type", "text/html; charset=UTF-8");
            response.body = html.getBytes(StandardCharsets.UTF_8);
            response.headers.put("Content-Length", String.valueOf(response.body.length));
            return response;
        }
        
        /**
         * Envía la respuesta HTTP al cliente
         */
        private void enviarRespuesta(OutputStream out, HttpResponse response) throws IOException {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            
            // Línea de estado
            writer.print("HTTP/1.1 " + response.statusCode + " " + response.statusMessage + "\r\n");
            
            // Headers por defecto
            response.headers.putIfAbsent("Server", "Java-HTTP-Server/2.0");
            response.headers.putIfAbsent("Date", getHttpDate());
            response.headers.putIfAbsent("Connection", "close");
            
            // Escribir headers
            for (Map.Entry<String, String> header : response.headers.entrySet()) {
                writer.print(header.getKey() + ": " + header.getValue() + "\r\n");
            }
            
            writer.print("\r\n");
            writer.flush();
            
            // Escribir body
            if (response.body != null) {
                out.write(response.body);
                out.flush();
            }
        }
        
        /**
         * Envía un error HTTP
         */
        private void enviarError(OutputStream out, int code, String message) throws IOException {
            String html = String.format(
                "<!DOCTYPE html><html><head><title>%d %s</title></head>" +
                "<body><h1>%d - %s</h1></body></html>", 
                code, message, code, message);
            
            HttpResponse response = new HttpResponse();
            response.statusCode = code;
            response.statusMessage = message;
            response.headers.put("Content-Type", "text/html; charset=UTF-8");
            response.body = html.getBytes(StandardCharsets.UTF_8);
            response.headers.put("Content-Length", String.valueOf(response.body.length));
            
            enviarRespuesta(out, response);
        }
        
        /**
         * Obtiene la extensión del archivo
         */
        private String getExtension(String filename) {
            int dot = filename.lastIndexOf('.');
            return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "";
        }
        
        /**
         * Formatea la fecha según el estándar HTTP
         */
        private String getHttpDate() {
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.format(new Date());
        }
    }
    
    /**
     * Clase para representar una petición HTTP
     */
    static class HttpRequest {
        String method;
        String path;
        String version;
        Map<String, String> headers = new HashMap<>();
    }
    
    /**
     * Clase para representar una respuesta HTTP
     */
    static class HttpResponse {
        int statusCode;
        String statusMessage;
        Map<String, String> headers = new HashMap<>();
        byte[] body;
    }
}
