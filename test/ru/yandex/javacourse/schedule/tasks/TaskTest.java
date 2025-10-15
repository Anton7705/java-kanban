package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TaskTest {

    @Test
    public void testIdIsImmutable(){
        Task t = new Task( "Test 1", "Testing task 1", TaskStatus.NEW);
        t.isManagedTrue();
        assertThrows(IllegalStateException.class, () -> t.setId(100));
    }

    @Test
    public void taskTypeShouldBeTask() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
        assertEquals(TaskType.TASK, task.getType());
    }
}
