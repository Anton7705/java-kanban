package ru.yandex.javacourse.schedule.tasks;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(String name, String description, TaskStatus status, int epicId) {
		super(name, description, status);
		if (this.id == epicId) {
			throw new IllegalArgumentException("Subtask cannot be attached to itself");
		}
		this.epicId = epicId;
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
}

