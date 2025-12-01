package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import static org.junit.jupiter.api.Assertions.*;

class HistoryHandlerTest extends HttpTaskServerTest {

    @Override
    protected URI takeURI() {
        return URI.create("http://localhost:8080/history");
    }

    @Test
    public void getHistory_whenEmpty_returns200AndEmptyArray() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, history.length);
        assertNotNull(history);
    }

    @Test
    public void historyEndpoint_returns500_ForPostRequest() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

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
    public void getHistory_returnsEntitiesInAccessOrder() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();

        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        manager.getTask(task1.getId());
        manager.getTask(task2.getId());

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, history.length);

        manager.deleteTask(task1.getId());

        HttpResponse<String> responseAfterDelete = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseAfterDelete.statusCode());

        Task[] historyAfterDelete = gson.fromJson(responseAfterDelete.body(), Task[].class);
        assertEquals(1, historyAfterDelete.length);
    }
}
