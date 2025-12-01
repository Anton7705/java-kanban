package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.managerExceptions.ManagerNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                List<Subtask> list = taskManager.getSubtasks();
                String jsonString = gson.toJson(list);
                sendText(exchange, jsonString);
                break;
            }
            case GET_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                Subtask task = taskManager.getSubtask(id);
                String jsonString = gson.toJson(task);
                sendText(exchange, jsonString);
                break;
            }
            case POST_TASK: {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Subtask task = gson.fromJson(body, Subtask.class);

                if (task.getId() == 0) {
                    taskManager.addNewSubtask(task);
                    sendCreated(exchange, "Подзадача создана с id = " + task.getId());
                }
                else {
                    taskManager.updateSubtask(task);
                    sendCreated(exchange, "Подзадача обновлена");
                }
                break;
            }
            case DELETE_TASKS: {
                taskManager.deleteSubtasks();
                sendText(exchange, "Все подзадачи успешно удалены");
                break;
            }
            case DELETE_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                taskManager.deleteSubtask(id);
                sendText(exchange, "Подзадача с id = " + id + " успешно удалена");
                break;
            }
            default:
                throw new ManagerNotFoundException("Эндпоинт не найден");
        }
    }

    @Override
    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length < 2 || !"subtasks".equals(pathParts[1])) {
            return Endpoint.UNKNOWN;
        }

        if (requestMethod.equals("GET")) {
            if (pathParts.length == 3) {
                return Endpoint.GET_TASK_BY_ID;
            }
            return Endpoint.GET_TASKS;
        }

        if (requestMethod.equals("POST")) {
            return Endpoint.POST_TASK;
        }

        if (requestMethod.equals("DELETE")) {
            if (pathParts.length == 3) {
                return Endpoint.DELETE_TASK_BY_ID;
            }
            return Endpoint.DELETE_TASKS;
        }
        return Endpoint.UNKNOWN;
    }
}
