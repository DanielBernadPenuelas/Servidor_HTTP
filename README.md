#  Java Multithreaded HTTP Server

![Java Version](https://img.shields.io/badge/Java-11%2B-orange?style=for-the-badge&logo=java)
![Architecture](https://img.shields.io/badge/Architecture-Sockets%20%26%20Thread%20Pool-blue?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

Un servidor web HTTP/1.1 multihilo desarrollado desde cero en **Java básico (Core Java)** sin librerías ni frameworks externos. Implementa gestión de sockets, arquitectura concurrente mediante un pool de hilos, procesamiento de peticiones HTTP, rutas de API y entrega de archivos estáticos con manejo de tipos MIME y seguridad.

---

##  Características Principales

-  **Arquitectura Concurrente (Multihilo):** Utiliza un pool fijo de 10 hilos (`ExecutorService`) para gestionar múltiples conexiones simultáneas de forma eficiente sin bloquear el hilo principal.
-  **Servidor de Archivos Estáticos:** Servidor integrado para entregar archivos (`HTML`, `CSS`, `JS`, `JSON`, imágenes, `PDF`, etc.) desde el directorio público `public/`.
-  **Parsing y Respuestas HTTP Manuales:** Implementación nativa para la lectura, desestructuración (headers, métodos, rutas) y construcción del protocolo HTTP/1.1.
-  **Endpoints REST Básicos:**
  - `/api/time`: Devuelve la hora del servidor y la marca de tiempo en formato JSON.
  - `/api/info`: Proporciona detalles del servidor y la versión de Java en formato JSON.
-  **Seguridad Integrada:** Protección contra ataques de tipo *Directory Traversal* saneando las rutas de las peticiones.
-  **Auto-inicialización:** Genera automáticamente la carpeta de recursos estáticos `public/` y un archivo `index.html` de prueba en el primer arranque si no existen.
-  **Puerto Configurable:** Puerto por defecto `8080`, modificable pasando el argumento en línea de comandos[cite: 1].
-  **Logging:** Registra eventos, solicitudes entrantes y errores mediante `java.util.logging.Logger`[cite: 1].

---

##  Tecnologías Utilizadas

- **Lenguaje:** Java 11+[cite: 1]
- **API Sockets:** `java.net.ServerSocket`, `java.net.Socket`[cite: 1]
- **Concurrencia:** `java.util.concurrent.ExecutorService`, `Executors`[cite: 1]
- **E/S y Archivos:** `java.nio.file.Files`, `java.nio.file.Path`, `BufferedReader`, `OutputStream`[cite: 1]

---

