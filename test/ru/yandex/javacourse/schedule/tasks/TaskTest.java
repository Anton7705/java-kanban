package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testIsTimeOverlap() {
        LocalDateTime localDateTime = LocalDateTime.now();
        int min = 90;
        Task taskBefore = new Task("Test 1", "Testing task 1", TaskStatus.NEW, min, localDateTime);
        Task taskAfter = new Task("Test 2", "Testing task 2", TaskStatus.NEW, min, localDateTime.plusMinutes(min));
        Task taskOverlap = new Task("Test 3", "Testing task 3", TaskStatus.NEW, min, localDateTime);
        assertFalse(taskBefore.isTimeOverlap(taskAfter));
        assertTrue(taskBefore.isTimeOverlap(taskOverlap));
    }

    @Test
    public void testGetEndTime() {
        LocalDateTime localDateTime = LocalDateTime.now();
        int min = 90;
        Task taskBefore = new Task("Test 1", "Testing task 1", TaskStatus.NEW, min, localDateTime);
        assertEquals(localDateTime, taskBefore.getStartTime().get());
        assertEquals(localDateTime.plusMinutes(min), taskBefore.getEndTime().get());
    }

}
