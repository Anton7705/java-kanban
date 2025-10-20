package ru.yandex.javacourse.schedule.manager;

import java.nio.file.Path;

/**
 * Default managers.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class Managers {
	public static TaskManager getDefault() {
		return new InMemoryTaskManager();
	}

	public static HistoryManager getDefaultHistory() {
		return new InMemoryHistoryManager();
	}

	public static FileBackedTaskManager getDefaultFileBackedTaskManager(Path path) {
		return new FileBackedTaskManager(path);
	}
}
