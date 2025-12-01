package ru.yandex.javacourse.schedule.http;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.managerExceptions.ManagerNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class EpicsHandlerTest extends HttpTaskServerTest {

    @Override
    protected URI takeURI() {
        return URI.create("http://localhost:8080/epics");
    }

    @Test
    void getAllEpics_returnsEmptyListAnd200_WhenNoEpics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(epics);
        assertEquals(0, epics.length);
    }

    @Test
    void getAllEpics_returnsAllEpicsAnd200_WhenEpicsExist() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Description 1");
        Epic epic2 = new Epic("Epic 2", "Description 2");

        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length);
        assertEquals("Epic 1", epics[0].getName());
        assertEquals("Epic 2", epics[1].getName());
    }

    @Test
    void getEpicById_returnsEpicAnd200_WhenEpicExists() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        URI uriWithId = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(retrievedEpic);
        assertEquals(epicId, retrievedEpic.getId());
        assertEquals("Test Epic", retrievedEpic.getName());
        assertEquals("Test Description", retrievedEpic.getDescription());
        assertEquals(TaskStatus.NEW, retrievedEpic.getStatus());
    }

    @Test
    void getEpicById_returns404_WhenEpicNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void getEpicSubtasks_returnsSubtasksListAnd200_WhenEpicHasSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epicId, 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.IN_PROGRESS, epicId, 45);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        URI uriSubtasks = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriSubtasks)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
        assertEquals("Subtask 1", subtasks[0].getName());
        assertEquals("Subtask 2", subtasks[1].getName());
        assertEquals(epicId, subtasks[0].getEpicId());
        assertEquals(epicId, subtasks[1].getEpicId());
    }

    @Test
    void getEpicSubtasks_returnsEmptyListAnd200_WhenEpicHasNoSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        URI uriSubtasks = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriSubtasks)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks);
        assertEquals(0, subtasks.length);
    }

    @Test
    void getEpicSubtasks_returns404_WhenEpicNotFound() throws IOException, InterruptedException {
        URI uriSubtasks = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriSubtasks)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void createEpic_returns201_WhenValidEpic() throws IOException, InterruptedException {
        Epic newEpic = new Epic("New Epic", "New Description");
        String epicJson = gson.toJson(newEpic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Эпик создан с id ="));

        List<Epic> epics = manager.getEpics();
        assertEquals(1, epics.size());
        assertEquals("New Epic", epics.get(0).getName());
    }

    @Test
    void deleteEpicById_returns200AndDeletesEpic_WhenEpicExists() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic to delete", "Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        URI uriWithId = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Эпик с id = " + epicId + " успешно удален"));

        assertThrows(ManagerNotFoundException.class, () -> manager.getEpic(epicId));
        assertEquals(0, manager.getEpics().size());
    }

    @Test
    void deleteEpicById_alsoDeletesSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic with subtasks", "Description");
        manager.addNewEpic(epic);
        int epicId = epic.getId();

        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW, epicId, 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.DONE, epicId, 45);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getSubtasks().size());

        URI uriWithId = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertThrows(ManagerNotFoundException.class, () -> manager.getEpic(epicId));
        assertEquals(0, manager.getSubtasks().size());
    }

    @Test
    void deleteEpicById_returns404_WhenEpicNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteAllEpics_returns200AndClearsAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Desc 1");
        Epic epic2 = new Epic("Epic 2", "Desc 2");
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask 1", "Desc", TaskStatus.NEW, epic1.getId(), 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc", TaskStatus.DONE, epic2.getId(), 45);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(2, manager.getEpics().size());
        assertEquals(2, manager.getSubtasks().size());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Все эпики успешно удалены"));

        assertEquals(0, manager.getEpics().size());
        assertEquals(0, manager.getSubtasks().size());
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
    void handleRequest_returns404_ForInvalidHttpMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode()); // Ожидаем 404, а не 500
        assertTrue(response.body().contains("Эндпоинт не найден"));
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