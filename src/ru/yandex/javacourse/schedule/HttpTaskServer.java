package ru.yandex.javacourse.schedule;

import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handler.*;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

public class HttpTaskServer {

    private static final URI BASE_URI = URI.create("http://localhost:8080");
    private static final int PORT = BASE_URI.getPort();
    private final TaskManager taskManager;


    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) throws IOException {
        taskManager = manager;

        httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer server = new HttpTaskServer();
        server.start();
    }
}
