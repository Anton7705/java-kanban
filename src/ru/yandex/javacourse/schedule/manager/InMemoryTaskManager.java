package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;
import static ru.yandex.javacourse.schedule.tasks.TaskType.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import ru.yandex.javacourse.schedule.managerExceptions.ManagerValidateException;
import ru.yandex.javacourse.schedule.tasks.*;

public class InMemoryTaskManager implements TaskManager {

	protected final Map<Integer, Task> tasks = new HashMap<>();
	protected final Map<Integer, Epic> epics = new HashMap<>();
	protected final Map<Integer, Subtask> subtasks = new HashMap<>();
	protected int generatorId = 0;
	private final HistoryManager historyManager = Managers.getDefaultHistory();
	Set<Task> taskTreeSet = new TreeSet<>(Comparator.comparing(task -> task.getStartTime().get()));


	protected void updateEpicTimes(int epicId) {
		Epic epic = epics.get(epicId);
		if (epic == null) {
			throw new IllegalArgumentException("Epic with id = " + epicId + " is not exists");
		}
		List<Subtask> epicSubtasks = getEpicSubtasks(epicId);

		if (epicSubtasks.isEmpty()) {
			epic.setDuration(Duration.ZERO);
			epic.setStartTime(null);
			epic.setEndTime(null);
			return;
		}

		Duration duration = epicSubtasks.stream()
				.map(Task::getDuration)
				.filter(Objects::nonNull)
				.reduce(Duration.ZERO, (Duration::plus));

		LocalDateTime startTime = epicSubtasks.stream()
				.map(Task::getStartTime)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.min(LocalDateTime::compareTo)
				.orElse(null);

		LocalDateTime endTime = epicSubtasks.stream()
				.map(Task::getEndTime)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.max(LocalDateTime::compareTo)
				.orElse(null);

		epic.setDuration(duration);
		epic.setStartTime(startTime);
		epic.setEndTime(endTime);
	}

    private void validateNoTimeOverlap(Task newTask) {
        if (newTask.getStartTime().isEmpty()) {
            return;
        }
        if (taskTreeSet.stream().anyMatch(existingTask -> existingTask.isTimeOverlap(newTask))) {
            throw new ManagerValidateException("The task overlaps with existing tasks");
        }
    }


	@Override
	public List<Task> getPrioritizedTasks() {
		return new ArrayList<>(taskTreeSet);
	}

	private void startTimeExists(Task task) {
        if (task.getStartTime().isPresent()) {
            taskTreeSet.add(task);
        }
	}


	@Override
	public ArrayList<Task> getTasks() {
		return new ArrayList<>(this.tasks.values());
	}

	@Override
	public ArrayList<Subtask> getSubtasks() {
		return new ArrayList<>(subtasks.values());
	}

	@Override
	public ArrayList<Epic> getEpics() {
		return new ArrayList<>(epics.values());
	}

	@Override
	public ArrayList<Subtask> getEpicSubtasks(int epicId) {
		ArrayList<Subtask> tasks = new ArrayList<>();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		for (int id : epic.getSubtaskIds()) {
			tasks.add(subtasks.get(id));
		}
		return tasks;
	}

	@Override
	public Task getTask(int id) {
		final Task task = tasks.get(id);
		historyManager.addTask(task);
		return task;
	}

	@Override
	public Subtask getSubtask(int id) {
		final Subtask subtask = subtasks.get(id);
		historyManager.addTask(subtask);
		return subtask;
	}

	@Override
	public Epic getEpic(int id) {
		final Epic epic = epics.get(id);
		historyManager.addTask(epic);
		return epic;
	}

	@Override
	public int addNewTask(Task task) {
		final int id = ++generatorId;
		task.setId(id);
		task.isManagedTrue();
        validateNoTimeOverlap(task);
        startTimeExists(task);
		tasks.put(id, task);
		return id;
	}

	@Override
	public int addNewEpic(Epic epic) {
		final int id = ++generatorId;
		epic.setId(id);
		epic.isManagedTrue();
		epics.put(id, epic);
		updateEpicTimes(id);
		return id;
	}

	@Override
	public Integer addNewSubtask(Subtask subtask) {
		final int epicId = subtask.getEpicId();
		Epic epic = epics.get(epicId);
		if (epic == null) {
			return null;
		}
		final int id = ++generatorId;
		subtask.setId(id);
		subtask.isManagedTrue();
        validateNoTimeOverlap(subtask);
        startTimeExists(subtask);
		subtasks.put(id, subtask);
		epic.addSubtaskId(subtask.getId());
		updateEpicStatus(epicId);
		updateEpicTimes(epicId);
		return id;
	}

	@Override
	public void updateTask(Task task) {
		final int id = task.getId();
		if (id == 0) {
			return;
		}
		final Task savedTask = tasks.get(id);
		if (savedTask == null) {
			return;
		}
        taskTreeSet.remove(task);
        validateNoTimeOverlap(task);
        startTimeExists(task);
		tasks.put(id, task);
	}

	@Override
	public void updateEpic(Epic epic) {
		final Epic savedEpic = epics.get(epic.getId());
		savedEpic.setName(epic.getName());
		savedEpic.setDescription(epic.getDescription());
	}

	@Override
	public void updateSubtask(Subtask subtask) {
		final int id = subtask.getId();
		final int epicId = subtask.getEpicId();
		final Subtask savedSubtask = subtasks.get(id);
		if (savedSubtask == null) {
			return;
		}
		final Epic epic = epics.get(epicId);
		if (epic == null) {
			return;
		}
        taskTreeSet.remove(subtask);
        validateNoTimeOverlap(subtask);
        startTimeExists(subtask);
		subtasks.put(id, subtask);
		updateEpicTimes(epicId);
		updateEpicStatus(epicId);
	}

	@Override
	public void deleteTask(int id) {
		historyManager.remove(id);
		Task removedTask = tasks.remove(id);
        taskTreeSet.remove(removedTask);
	}

	@Override
	public void deleteEpic(int id) {
		historyManager.remove(id);
		final Epic epic = epics.remove(id);
		for (Integer subtaskId : epic.getSubtaskIds()) {
            deleteSubtask(subtaskId);
		}
	}

	@Override
	public void deleteSubtask(int id) {
		historyManager.remove(id);
		Subtask subtask = subtasks.remove(id);
		if (subtask == null) {
			return;
		}
		Epic epic = epics.get(subtask.getEpicId());
		epic.removeSubtask(id);
        taskTreeSet.remove(subtask);
		updateEpicTimes(epic.getId());
		updateEpicStatus(epic.getId());
	}

	@Override
	public void deleteTasks() {
		historyManager.removeAll(TASK);
        taskTreeSet.removeAll(tasks.values());
		tasks.clear();
	}

	@Override
	public void deleteSubtasks() {
		historyManager.removeAll(SUBTASK);
		for (Epic epic : epics.values()) {
			epic.cleanSubtaskIds();
			updateEpicStatus(epic.getId());
			updateEpicTimes(epic.getId());
		}
        taskTreeSet.removeAll(subtasks.values());
		subtasks.clear();
	}

	@Override
	public void deleteEpics() {
        taskTreeSet.removeAll(subtasks.values());
		historyManager.removeAll(SUBTASK);
		historyManager.removeAll(EPIC);
		epics.clear();
		subtasks.clear();
	}

	@Override
	public List<Task> getHistory() {
		return historyManager.getHistory();
	}

	private void updateEpicStatus(int epicId) {
		Epic epic = epics.get(epicId);
		List<Integer> subs = epic.getSubtaskIds();
		if (subs.isEmpty()) {
			epic.setStatus(NEW);
			return;
		}
		TaskStatus status = null;
		for (int id : subs) {
			final Subtask subtask = subtasks.get(id);
			if (status == null) {
				status = subtask.getStatus();
				continue;
			}

			if (status == subtask.getStatus()
					&& status != IN_PROGRESS) {
				continue;
			}
			epic.setStatus(IN_PROGRESS);
			return;
		}
		epic.setStatus(status);
	}
}
