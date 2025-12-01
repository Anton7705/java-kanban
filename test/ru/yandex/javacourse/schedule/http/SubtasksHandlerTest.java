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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.util.List;


class SubtasksHandlerTest extends HttpTaskServerTest {

    private Epic testEpic;

    @Override
    protected URI takeURI() {
        return URI.create("http://localhost:8080/subtasks");
    }

    @BeforeEach
    @Override
    protected void setUp() throws IOException {
        super.setUp();

        testEpic = new Epic("Test Epic", "Epic for subtasks");
        manager.addNewEpic(testEpic);
    }

    @Test
    void getAllSubtasks_returnsEmptyListAnd200_WhenNoSubtasks() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks);
        assertEquals(0, subtasks.length);
    }

    @Test
    void getAllSubtasks_returnsAllSubtasksAnd200_WhenSubtasksExist() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", TaskStatus.NEW,
                testEpic.getId(), 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", TaskStatus.IN_PROGRESS,
                testEpic.getId(), 45);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);

        assertEquals(testEpic.getId(), subtasks[0].getEpicId());
        assertEquals(testEpic.getId(), subtasks[1].getEpicId());
    }

    @Test
    void getSubtaskById_returnsSubtaskAnd200_WhenSubtaskExists() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Test Description", TaskStatus.NEW,
                testEpic.getId(), 60);
        manager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        URI uriWithId = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(retrievedSubtask);
        assertEquals(subtaskId, retrievedSubtask.getId());
        assertEquals("Test Subtask", retrievedSubtask.getName());
        assertEquals("Test Description", retrievedSubtask.getDescription());
        assertEquals(TaskStatus.NEW, retrievedSubtask.getStatus());
        assertEquals(testEpic.getId(), retrievedSubtask.getEpicId());
    }

    @Test
    void getSubtaskById_returns404_WhenSubtaskNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("не найден") || response.body().contains("NotFound"));
    }

    @Test
    void getSubtaskById_returns404_WhenEpicIdProvidedInsteadOfSubtask() throws IOException, InterruptedException {
        URI uriWithEpicId = URI.create("http://localhost:8080/subtasks/" + testEpic.getId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithEpicId)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void createSubtask_returns201_WhenValidSubtask() throws IOException, InterruptedException {
        Subtask newSubtask = new Subtask("New Subtask", "New Description", TaskStatus.NEW,
                testEpic.getId(), 90);
        String subtaskJson = gson.toJson(newSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Подзадача создана с id ="));

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals("New Subtask", subtasks.get(0).getName());
        assertEquals(testEpic.getId(), subtasks.get(0).getEpicId());

        Epic updatedEpic = manager.getEpic(testEpic.getId());
        assertNotNull(updatedEpic);
    }

    @Test
    void createSubtaskWithStartTime_returns201_WhenValidSubtask() throws IOException, InterruptedException {
        Subtask newSubtask = new Subtask("New Subtask", "New Description", TaskStatus.NEW,
                testEpic.getId(), 90, LocalDateTime.now().plusHours(2));
        String subtaskJson = gson.toJson(newSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertTrue(subtasks.get(0).getStartTime().isPresent());
    }

    @Test
    void createSubtask_returns404_WhenEpicNotFound() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.NEW,
                999, 30);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void updateSubtask_returns201_WhenValidUpdate() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Original Subtask", "Original Description", TaskStatus.NEW,
                testEpic.getId(), 60);
        manager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        subtask.setName("Updated Subtask");
        subtask.setDescription("Updated Description");
        subtask.setStatus(TaskStatus.DONE);
        String updatedSubtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Подзадача обновлена"));

        Subtask updatedSubtask = manager.getSubtask(subtaskId);
        assertEquals("Updated Subtask", updatedSubtask.getName());
        assertEquals("Updated Description", updatedSubtask.getDescription());
        assertEquals(TaskStatus.DONE, updatedSubtask.getStatus());

        Epic updatedEpic = manager.getEpic(testEpic.getId());
        assertEquals(TaskStatus.DONE, updatedEpic.getStatus());
    }

    @Test
    void deleteSubtaskById_returns200AndDeletesSubtask_WhenSubtaskExists() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask to delete", "Description", TaskStatus.NEW,
                testEpic.getId(), 30);
        manager.addNewSubtask(subtask);
        int subtaskId = subtask.getId();

        URI uriWithId = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("успешно удалена"));

        assertThrows(ManagerNotFoundException.class, () -> manager.getSubtask(subtaskId));
        assertEquals(0, manager.getSubtasks().size());

        Epic updatedEpic = manager.getEpic(testEpic.getId());
        assertNotNull(updatedEpic);
    }

    @Test
    void deleteSubtaskById_returns404_WhenSubtaskNotFound() throws IOException, InterruptedException {
        URI uriWithId = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uriWithId)
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    void deleteAllSubtasks_returns200AndClearsAllSubtasks() throws IOException, InterruptedException {
        Subtask subtask1 = new Subtask("Subtask 1", "Desc 1", TaskStatus.NEW,
                testEpic.getId(), 30);
        Subtask subtask2 = new Subtask("Subtask 2", "Desc 2", TaskStatus.DONE,
                testEpic.getId(), 45);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        assertEquals(2, manager.getSubtasks().size());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Все подзадачи успешно удалены"));

        assertEquals(0, manager.getSubtasks().size());

        Epic updatedEpic = manager.getEpic(testEpic.getId());
        assertTrue(updatedEpic.getSubtaskIds().isEmpty());
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

        assertEquals(404, response.statusCode());
    }

    @Test
    void createSubtask_returns406_WhenSubtaskHasTimeConflict() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Subtask firstSubtask = new Subtask("Subtask 1", "Description", TaskStatus.NEW,
                testEpic.getId(), 60, startTime);
        manager.addNewSubtask(firstSubtask);

        Subtask conflictingSubtask = new Subtask("Subtask 2", "Description", TaskStatus.NEW,
                testEpic.getId(), 30, startTime.plusMinutes(30));
        String subtaskJson = gson.toJson(conflictingSubtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
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

    @Test
    void subtaskCreation_updatesEpicStatusCorrectly() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask", "Description", TaskStatus.IN_PROGRESS,
                testEpic.getId(), 60);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Epic updatedEpic = manager.getEpic(testEpic.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getStatus());
    }

    @Test
    void createSubtask_WithoutDuration_returns201() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Subtask no duration", "Description", TaskStatus.NEW,
                testEpic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(takeURI())
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Subtask> subtasks = manager.getSubtasks();
        assertEquals(1, subtasks.size());
        assertEquals(Duration.ZERO, subtasks.get(0).getDuration());
    }
}