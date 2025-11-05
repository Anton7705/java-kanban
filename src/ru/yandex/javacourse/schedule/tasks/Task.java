package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
	protected int id;
	protected String name;
	protected TaskStatus status;
	protected String description;
	protected boolean isManaged = false;
	protected Duration duration = Duration.ZERO;
	protected LocalDateTime startTime;

	public Task(String name, String description, TaskStatus status) {
		this.name = name;
		this.description = description;
		this.status = status;
	}

	public Task(String name, String description, TaskStatus status, long minutes) {
		this(name, description, status);
		this.duration = Duration.ofMinutes(minutes);
	}

	public Task(String name, String description, TaskStatus status, long minutes, LocalDateTime startTime) {
		this(name, description, status, minutes);
		this.startTime = startTime;
	}

	public Optional<LocalDateTime> getEndTime() {
		if (startTime == null || duration == null) {
			return Optional.empty();
		}
		return Optional.of(startTime.plus(duration));
	}

	public Optional<LocalDateTime> getStartTime() {
		return Optional.ofNullable(startTime);
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		if (duration == null) {
			throw new IllegalArgumentException();
		}
		this.duration = duration;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public boolean isTimeOverlap(Task other) {
		if (this == other) return false;
		if (this.startTime == null || other.startTime == null) return false;

		LocalDateTime thisEnd = this.getEndTime().get();
		LocalDateTime otherEnd = other.getEndTime().get();

		return this.startTime.isBefore(otherEnd) && other.startTime.isBefore(thisEnd);
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
				"," + description +
				"," + duration.toMinutes() +
				"," + startTime;
	}

}
