package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.DONE;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

public class Main {
	public static void main(String[] args) {

		TaskManager manager = Managers.getDefault();

		// Создание
		Task task1 = new Task("Task #1", "Task1 description", NEW);
		Task task2 = new Task("Task #2", "Task2 description", IN_PROGRESS);
		final int taskId1 = manager.addNewTask(task1);
		final int taskId2 = manager.addNewTask(task2);

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

		manager.getEpic(epicId1);
		manager.getSubtask(subtask3.getId());
		manager.getEpic(epicId2);
		manager.getSubtask(subtask2.getId());
		manager.getSubtask(subtask1.getId());
		manager.getTask(task1.getId());

		System.out.println("История 1 :");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}

		manager.getTask(task1.getId());
		manager.getSubtask(subtask2.getId());
		manager.getSubtask(subtask3.getId());

		System.out.println("История 2 :");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}

		manager.deleteTask(taskId1);
		manager.deleteEpic(epicId2);

		System.out.println("История 3 :");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}

		manager.deleteEpic(epicId1);

		System.out.println("История 4 :");
		for (Task task : manager.getHistory()) {
			System.out.println(task);
		}
	}
}
