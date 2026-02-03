package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.managerExceptions.ManagerNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TasksHandlerTest extends HttpTaskServerTest {

    @Override
    protected URI takeURI() {
        return URI.create("http://localhost:8080/tasks");
    }

    @Test
    void getAllTasks_returnsEmptyListAnd200_WhenNoTasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks);
        assertEquals(0, tasks.length);
    }

    @Test
    void getAllTasks_returnsAllTasksAnd200_WhenTasksExist() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", TaskStatus.NEW,
                30, LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("Task 2", "Description 2", TaskStatus.IN_PROGRESS,
                45, LocalDateTime.now().plusMinutes(50));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);

        assertTrue(tasks[0].getName().contains("Task"));
        assertTrue(tasks[1].getName().contains("Task"));
    }

    @Test
    void getTaskById_returnsTaskAnd200_WhenTaskExists() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW,
                60, LocalDateTime.now().plusHours(1));
        manager.addNewTask(task);
        int taskId = task.getId();

        URI uriWithId = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(retrievedTask);
        assertEquals(taskId, retrievedTask.getId());
        assertEquals("Test Task", retrievedTask.getName());
        assertEquals("Test Description", retrievedTask.getDescription());
        assertEquals(TaskStatus.NEW, retrievedTask.getStatus());
    }

    @Test
    void getTaskById_returns404_WhenTaskNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("не найден") || response.body().contains("NotFound"));
    }

    @Test
    void createTask_returns201_WhenValidTask() throws IOException, InterruptedException {
        Task newTask = new Task("New Task", "New Description", TaskStatus.NEW,
                90, LocalDateTime.now().plusHours(2));
        String taskJson = gson.toJson(newTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Задача создана с id ="));

        List<Task> tasks = manager.getTasks();
        assertEquals(1, tasks.size());
        assertEquals("New Task", tasks.get(0).getName());
    }

    @Test
    void updateTask_returns201_WhenValidUpdate() throws IOException, InterruptedException {
        Task task = new Task("Original Task", "Original Description", TaskStatus.NEW,
                60, LocalDateTime.now().plusHours(1));
        manager.addNewTask(task);
        int taskId = task.getId();

        task.setDescription("Updated Description");
        task.setStatus(TaskStatus.IN_PROGRESS);
        String updatedTaskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Задача обновлена"));

        Task updatedTask = manager.getTask(taskId);
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    void deleteTaskById_returns200AndDeletesTask_WhenTaskExists() throws IOException, InterruptedException {
        Task task = new Task("Task to delete", "Description", TaskStatus.NEW,
                30, LocalDateTime.now().plusMinutes(30));
        manager.addNewTask(task);
        int taskId = task.getId();

        URI uriWithId = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("успешно удалена"));

        assertThrows(ManagerNotFoundException.class, () -> manager.getTask(taskId));
        assertEquals(0, manager.getTasks().size());
    }

    @Test
    void deleteTaskById_returns404_WhenTaskNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteAllTasks_returns200AndClearsAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", TaskStatus.NEW, 30, LocalDateTime.now().plusMinutes(10));
        Task task2 = new Task("Task 2", "Desc 2", TaskStatus.DONE, 45, LocalDateTime.now().plusMinutes(50));
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        assertEquals(2, manager.getTasks().size());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Все задачи успешно удалены"));

        assertEquals(0, manager.getTasks().size());
    }

    @Test
    void handleRequest_returns404_ForInvalidEndpoint() throws IOException, InterruptedException {
        URI invalidUri = URI.create("http://localhost:8080/invalid");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(invalidUri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void createTask_returns406_WhenTaskHasTimeConflict() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Task firstTask = new Task("Task 1", "Description", TaskStatus.NEW,
                60, startTime);
        manager.addNewTask(firstTask);

        Task conflictingTask = new Task("Task 2", "Description", TaskStatus.NEW,
                30, startTime.plusMinutes(30));
        String taskJson = gson.toJson(conflictingTask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
    }

    @Test
    void handleRequest_returns500_ForInvalidJsonInPostRequest() throws IOException, InterruptedException {
        String invalidJson = "{invalid json}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("Непредвиденная ошибка"));
    }
}