package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.tasks.TaskType.*;

@DisplayName("Менеджер истории задач")
public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    @DisplayName("Не добавлять дубликат задачи в историю")
    public void testHistoricVersions() {
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be not added");
    }

    @Test
    @DisplayName("Обновлять задачу в истории при повторном добавлении")
    public void testHistoricVersionsByPointer() {
        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().get(0).getStatus(), "historic task should be changed");
    }


    @Test
    @DisplayName("Заменять предыдущую задачу при добавлении с одинаковым ID")
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
    @DisplayName("Добавлять задачи с разными ID в историю")
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
    @DisplayName("Удалять задачи из истории по ID")
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
    @DisplayName("Удалять все задачи из истории")
    public void testHistoricRemoveAllTasks() {
        Task task1 = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testiong task 2", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.removeAll(TASK);
        assertEquals(0, historyManager.getHistory().size(), "All tasks should be removed");
    }

    @Test
    @DisplayName("Удалять все эпики из истории")
    public void testHistoricRemoveAllEpics() {
        Epic epic1 = new Epic("Epic 1", "Testing epic 1");
        Epic epic2 = new Epic("Epic 2", "Testing epic 2");
        epic1.setId(1);
        epic2.setId(2);
        historyManager.addTask(epic1);
        historyManager.addTask(epic2);
        historyManager.removeAll(EPIC);
        assertEquals(0, historyManager.getHistory().size(), "All epics should be removed");
    }

    @Test
    @DisplayName("Удалять все подзадачи из истории")
    public void testHistoricRemoveAllSubtasks() {
        Subtask subtask1 = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        Subtask subtask2 = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        subtask1.setId(1);
        subtask2.setId(2);
        historyManager.addTask(subtask1);
        historyManager.addTask(subtask2);
        historyManager.removeAll(SUBTASK);
        assertEquals(0, historyManager.getHistory().size(), "All subtasks should be removed");
    }

    @Test
    @DisplayName("Удаление задачи из начала истории")
    public void testRemoveFromBeginning() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task("Test 3", "Testing task 3", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task1.getId());
        assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    @DisplayName("Удаление задачи из середины истории")
    public void testRemoveFromMiddle() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task("Test 3", "Testing task 3", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task2.getId());
        assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    @DisplayName("Удаление задачи из конца истории")
    public void testRemoveFromEnd() {
        Task task1 = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        Task task2 = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        Task task3 = new Task("Test 3", "Testing task 3", TaskStatus.NEW);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
        historyManager.addTask(task1);
        historyManager.addTask(task2);
        historyManager.addTask(task3);
        historyManager.remove(task3.getId());
        assertEquals(2, historyManager.getHistory().size());
    }
}
