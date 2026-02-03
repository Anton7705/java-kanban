package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            throw new RuntimeException("Метод " + exchange.getRequestMethod() + " не поддерживается для /prioritized");
        }
        sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()));
    }
}
