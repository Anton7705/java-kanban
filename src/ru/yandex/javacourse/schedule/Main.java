package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.FileBackedTaskManager;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
	public static void main(String[] args) {

//		Дополнительное задание. Реализуем пользовательский сценарий
		File tempFile;
		try {
			tempFile = File.createTempFile("task_manager_test", ".csv");
		} catch (IOException e) {
			throw new RuntimeException("Не удалось создать временный файл", e);
		}


		TaskManager manager = Managers.getDefaultFileBackedTaskManager(tempFile.toPath());

		// Создание
		Task task1 = new Task("Task #1", "Task1 description", NEW);
		Task task2 = new Task("Task #2", "Task2 description", IN_PROGRESS);
		final int taskId1 = manager.addNewTask(task1);

		System.out.println("Тест 1");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

		Epic epic1 = new Epic("Epic #1", "Epic1 description");
		Epic epic2 = new Epic("Epic #2", "Epic2 description");
		final int epicId1 = manager.addNewEpic(epic1);
		final int epicId2 = manager.addNewEpic(epic2);

		System.out.println("Тест 2");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

		Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
		Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1);
		Subtask subtask3 = new Subtask("Subtask #3-2", "Subtask1 description", DONE, epicId1);
		int subtaskId1 = manager.addNewSubtask(subtask1);
		int subtaskId2 = manager.addNewSubtask(subtask2);
		int subtaskId3 = manager.addNewSubtask(subtask3);

		System.out.println("Тест 3");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

		manager.deleteTask(taskId1);
		manager.deleteEpic(epicId2);

		System.out.println("Тест 4");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

		manager.deleteSubtask(subtaskId1);
		manager.deleteSubtask(subtaskId2);

		System.out.println("Тест 5");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

		manager.deleteEpic(epicId1);

		System.out.println("Тест 6");
		printAllTasks(FileBackedTaskManager.loadFromFile(tempFile));

	}
	private static void printAllTasks(TaskManager manager) {
		System.out.println("Задачи:");
		for (Task task : manager.getTasks()) {
			System.out.println(task);
		}
		System.out.println("Эпики:");
		for (Task epic : manager.getEpics()) {
			System.out.println(epic);
			for (Task task : manager.getEpicSubtasks(epic.getId())) {
				System.out.println("--> " + task);
			}
		}
		System.out.println("Подзадачи:");
		for (Task subtask : manager.getSubtasks()) {
			System.out.println(subtask);
		}
	}
}
