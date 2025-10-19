package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.*;

public class FileBackedTaskManagerTest {
    File tempFile;
    FileBackedTaskManager manager;

    @BeforeEach
    public void initManager() throws IOException {
        tempFile = File.createTempFile("task_manager_test", ".csv");
        manager = Managers.getDefaultFileBackedTaskManager(tempFile.toPath());
    }

    @DisplayName("Проверка создания файла по названию при вызове конструткора")
    @Test
    public void fileExistsTest() {
        File testFile = new File("test_path");
        new FileBackedTaskManager(testFile.toPath());
        assertTrue(testFile.exists());
    }

    @DisplayName("Проверка корректного текстового представления классов для сохранения в файл")
    @Test
    public void toStringForFileTest() {
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        manager.addNewTask(task1);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        final int epicId1 = manager.addNewEpic(epic1);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", DONE, epicId1);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);

        assertEquals("1,TASK,Task #1,NEW,Task1 description", task1.toStringForFile());
        assertEquals("3,SUBTASK,Subtask #1-1,NEW,Subtask1 description,2", subtask1.toStringForFile());
        assertEquals("2,EPIC,Epic #1,IN_PROGRESS,Epic1 description,[3, 4, 5]", epic1.toStringForFile());
    }

    @DisplayName("Парсинг строкового представления в объекты всех типов задач")
    @Test
    public void fromStringTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, 1);

        Class clz = FileBackedTaskManager.class;
        Method fromString = clz.getDeclaredMethod("fromString", String.class);
        fromString.setAccessible(true);
        Task parsedTask = (Task) fromString.invoke(new Object(), task1.toStringForFile());
        Task parsedEpic = (Task) fromString.invoke(new Object(), epic1.toStringForFile());
        Task parsedSubtask = (Task) fromString.invoke(new Object(), subtask1.toStringForFile());
        assertTrue(parsedTask.equals(task1));
        assertTrue(parsedEpic.equals(epic1));
        assertTrue(parsedSubtask.equals(subtask1));
    }


    @DisplayName("Восстановление состояния менеджера из файла после сохранения")
    @Test
    public void loadFromFileTest() {
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        final int taskId1 = manager.addNewTask(task1);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        final int epicId2 = manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
        int subtaskId1 = manager.addNewSubtask(subtask1);

        FileBackedTaskManager loadFromFile = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(task1.equals(loadFromFile.getTask(taskId1)));
        assertTrue(epic1.equals(loadFromFile.getEpic(epicId1)));
        assertTrue(epic2.equals(loadFromFile.getEpic(epicId2)));
        assertTrue(subtask1.equals(loadFromFile.getSubtask(subtaskId1)));
    }

    @DisplayName("Проверка последовательности и формата записи задач в CSV файл")
    @Test
    public void testFormatAndOrder() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(tempFile));
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        manager.addNewTask(task1);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        final int epicId2 = manager.addNewEpic(epic2);


        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", DONE, epicId1);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);

        assertEquals("id,type,name,status,description,epic/subtaskIds", bufferedReader.readLine());
        assertEquals(task1.toStringForFile(), bufferedReader.readLine());
        assertEquals(epic1.toStringForFile(), bufferedReader.readLine());
        assertEquals(epic2.toStringForFile(), bufferedReader.readLine());
        assertEquals(subtask1.toStringForFile(), bufferedReader.readLine());
        assertEquals(subtask2.toStringForFile(), bufferedReader.readLine());
        assertEquals(subtask3.toStringForFile(), bufferedReader.readLine());
    }
}
