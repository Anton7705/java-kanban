package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class Epic extends Task {
	protected Set<Integer> subtaskIds = new HashSet<>();
	protected LocalDateTime endTime;

	public Epic(String name, String description) {
		super(name, description, NEW);
	}

	public void addSubtaskId(int id) {
		if (id == this.id) {
			throw new IllegalArgumentException("Subtask cannot be attached to itself");
		}
		subtaskIds.add(id);
	}

	@Override
	public void setDuration(Duration duration) {
		this.duration = duration;
	}

    public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	@Override
	public Optional<LocalDateTime> getEndTime() {
		return Optional.ofNullable(endTime);
	}

	@Override
	public TaskType getType() {
		return TaskType.EPIC;
	}

	public List<Integer> getSubtaskIds() {
		return new ArrayList<>(subtaskIds);
	}

	public void cleanSubtaskIds() {
		subtaskIds.clear();
	}

	public void removeSubtask(int id) {
		subtaskIds.remove(id);
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIds +
				'}';
	}

	@Override
	public String toStringForFile() {
		return super.toStringForFile() + "," + subtaskIds;
	}
}
