package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskType;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {

	private final Map<Integer, Node> history = new HashMap<>();
	private Node head;
	private Node tail;


	@Override
	public void removeAll(TaskType type) {
		getHistory().stream()
				.filter(task -> task.getType() == type)
				.forEach(task -> remove(task.getId()));
	}

	@Override
	public List<Task> getHistory() {
		return getTasks();
	}

	@Override
	public void addTask(Task task) {
		if (task == null) {
			return;
		}
		remove(task.getId());
		history.put(task.getId(), linkLast(task));
	}

	@Override
	public void remove(int id) {
		Node node = history.get(id);
		if (node != null) {
			removeNode(node);
			history.remove(id);
		}
	}

	private Node linkLast(Task element) {
		final Node oldTail = tail;
		final Node newNode = new Node(oldTail, element, null);
		tail = newNode;
		if (oldTail == null) {
			head = newNode;
		} else {
			oldTail.next = newNode;
		}
		return newNode;
	}


	public List<Task> getTasks() {
		List<Task> list = new ArrayList<>();
		Node current = head;
		while (current != null) {
			list.add(current.data);
			current = current.next;
		}
		return list;
	}

	private void removeNode(Node node) {

		if (node.prev != null) {
			node.prev.next = node.next;
		} else {
			head = node.next;
		}

		if (node.next != null) {
			node.next.prev = node.prev;
		} else {
			tail = node.prev;
		}

	}

	class Node {
		private Task data;
		private Node next;
		private Node prev;

		public Node(Node prev, Task data, Node next) {
			this.data = data;
			this.next = next;
			this.prev = prev;
		}

	}

}
