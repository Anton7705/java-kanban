package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager(){
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions(){
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be not added");
    }

    @Test
    public void testHistoricVersionsByPointer(){
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be changed");
    }


    @Test
    public void shouldRemovePreviousTaskWhenAddingDuplicateId() {
        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(1);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        System.out.println(historyManager.getHistory());
        assertEquals(task2.getName(), "Test 2", "task with the same id should be replace previous task");
    }

    @Test
    public void testHistoricAddingTasksWithDifferentId() {
        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        assertEquals(2, historyManager.getHistory().size(), "task with the different id should be added");
    }

    @Test
    public void testHistoricRemoveTasksById() {
        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.remove(task1.getId());
        assertEquals(1, historyManager.getHistory().size(), "task should be removed");
        historyManager.remove(task2.getId());
        assertEquals(0, historyManager.getHistory().size(), "task should be removed");
    }

    @Test
    public void testHistoricRemoveAllTasks() {
        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.removeAll(Task.class);
        assertEquals(0, historyManager.getHistory().size(), "All tasks should be removed");
    }

    @Test
    public void testHistoricRemoveAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Testing epic 1");
        Epic epic2 = new Epic("Epic 2", "Testing epic 2");
        epic1.setId(1);
        epic2.setId(2);
        historyManager.addTask(epic1);
        historyManager.addTask(epic2);
        historyManager.removeAll(Epic.class);
        assertEquals(0, historyManager.getHistory().size(), "All epics should be removed");
    }

    @Test
    public void testHistoricRemoveAllSubtasks() {
        Subtask subtask1 = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        subtask1.setId(1);
        subtask2.setId(2);
        historyManager.addTask(subtask1);
        historyManager.addTask(subtask2);
        historyManager.removeAll(Subtask.class);
        assertEquals(0, historyManager.getHistory().size(), "All subtasks should be removed");
    }

}
