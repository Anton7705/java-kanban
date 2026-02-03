package ru.yandex.javacourse.schedule.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.managerExceptions.ManagerNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        Endpoint endpoint = getEndpoint(exchange.getRequestURI().getPath(), exchange.getRequestMethod());

        switch (endpoint) {
            case GET_TASKS: {
                List<Epic> list = taskManager.getEpics();
                String jsonString = gson.toJson(list);
                sendText(exchange, jsonString);
                break;
            }
            case GET_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                Epic task = taskManager.getEpic(id);
                String jsonString = gson.toJson(task);
                sendText(exchange, jsonString);
                break;
            }
            case GET_EPIC_SUBTASKS: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                List<Subtask> list = taskManager.getEpicSubtasks(id);
                String jsonString = gson.toJson(list);
                sendText(exchange, jsonString);
                break;
            }
            case POST_TASK: {
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Epic task = gson.fromJson(body, Epic.class);

                if (task.getId() == 0) {
                    taskManager.addNewEpic(task);
                    sendCreated(exchange, "Эпик создан с id = " + task.getId());
                } else {
                    taskManager.updateEpic(task);
                    sendCreated(exchange, "Эпик обновлен");
                }
                break;
            }
            case DELETE_TASKS: {
                taskManager.deleteEpics();
                sendText(exchange, "Все эпики успешно удалены");
                break;
            }
            case DELETE_TASK_BY_ID: {
                String[] pathParts = exchange.getRequestURI().getPath().split("/");
                int id = Integer.parseInt(pathParts[2]);

                taskManager.deleteEpic(id);
                sendText(exchange, "Эпик с id = " + id + " успешно удален");
                break;
            }
            default:
                throw new ManagerNotFoundException("Эндпоинт не найден");
        }
    }

    @Override
    protected Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length < 2 || !"epics".equals(pathParts[1])) {
            return Endpoint.UNKNOWN;
        }

        if (requestMethod.equals("GET")) {
            if (pathParts.length == 4 && "subtasks".equals(pathParts[3])) {
                return Endpoint.GET_EPIC_SUBTASKS;
            } else if (pathParts.length == 3) {
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


