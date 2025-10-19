package ru.yandex.javacourse.schedule.tasks;

import java.util.HashSet;
import java.util.Objects;

public class Task {
	protected int id;
	protected String name;
	protected TaskStatus status;
	protected String description;
	protected boolean isManaged = false;

	public Task(String name, String description, TaskStatus status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public TaskType getType() {
		return TaskType.TASK;
	}

	public void isManagedTrue() {
		isManaged = true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if (isManaged) {
			throw new IllegalStateException("Cannot change id of managed task");
		}
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Task task = (Task) o;
		return id == task.id;
	}

	@Override
	public String toString() {
		return "Task{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status='" + status + '\'' +
				", description='" + description + '\'' +
				'}';
	}

	public String toStringForFile() {
		return id +
				"," + getType() +
				"," + name +
				"," + status +
				"," + description;
	}

}
