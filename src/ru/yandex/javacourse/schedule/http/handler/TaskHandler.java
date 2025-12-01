package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.managerExceptions.ManagerNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                List<Task> list = taskManager.getTasks();
                String jsonString = gson.toJson(list);
                sendText(exchange, jsonString);
                break;
            }
            case GET_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                Task task = taskManager.getTask(id);
                String jsonString = gson.toJson(task);
                sendText(exchange, jsonString);
                break;
            }
            case POST_TASK: {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);

                if (task.getId() == 0) {
                    taskManager.addNewTask(task);
                    sendCreated(exchange, "Задача создана с id = " + task.getId());
                } else {
                    taskManager.updateTask(task);
                    sendCreated(exchange, "Задача обновлена");
                }
                break;
            }
            case DELETE_TASKS: {
                taskManager.deleteTasks();
                sendText(exchange, "Все задачи успешно удалены");
                break;
            }
            case DELETE_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                taskManager.deleteTask(id);
                sendText(exchange, "Задача с id = " + id + " успешно удалена");
                break;
            }
            default:
                throw new ManagerNotFoundException("Эндпоинт не найден");
        }
    }

    @Override
    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length < 2 || !"tasks".equals(pathParts[1])) {
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
