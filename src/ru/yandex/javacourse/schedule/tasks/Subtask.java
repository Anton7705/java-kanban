package ru.yandex.javacourse.schedule.tasks;

import java.time.LocalDateTime;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(String name, String description, TaskStatus status, int epicId) {
		super(name, description, status);
		checkEpicId(epicId);
		this.epicId = epicId;
	}
	public Subtask(String name, String description, TaskStatus status, int epicId, long minutes) {
		super(name, description, status, minutes);
		checkEpicId(epicId);
		this.epicId = epicId;
	}

	public Subtask(String name, String description, TaskStatus status, int epicId, long minutes, LocalDateTime startTime) {
		super(name, description, status, minutes, startTime);
		checkEpicId(epicId);
		this.epicId = epicId;
	}

	private void checkEpicId(int epicId) {
		if (this.id == epicId) {
			throw new IllegalArgumentException("Subtask cannot be attached to itself");
		}
	}

	public int getEpicId() {
		return epicId;
	}

	@Override
	public TaskType getType() {
		return TaskType.SUBTASK;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", epicId=" + epicId +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				'}';
	}


	@Override
	public String toStringForFile() {
		return super.toStringForFile() + "," + epicId;
	}
}

