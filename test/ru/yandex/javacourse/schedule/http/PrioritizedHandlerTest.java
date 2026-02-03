package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PrioritizedHandlerTest extends HttpTaskServerTest {

    @Override
    protected URI takeURI() {
        return URI.create("http://localhost:8080/prioritized");
    }

    @Test
    public void getTasks_returnsEmptyListAnd200_WhenNoTasksExist() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(takeURI()).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, tasks.length);
    }


    @Test
    public void prioritizedEndpoint_returns500_ForPostRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(takeURI()).POST(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }

    @Test
    public void historyEndpoint_returns500_ForDeleteRequest() throws IOException, InterruptedException {
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(takeURI())
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(500, response.statusCode());
    }

    @Test
    public void getPrioritized_returnsAllTasksInPriorityOrder() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();

        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW, 15, LocalDateTime.now());
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW, 90, LocalDateTime.now().plus(Duration.ofMinutes(100)));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
        assertTrue(tasks[0].getStartTime().get().isBefore(tasks[1].getStartTime().get()));
    }
}
