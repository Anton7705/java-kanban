package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SubtaskTest {

    @Test
    public void testIdIsImmutable(){
        Subtask subtask = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        subtask.isManagedTrue();
        assertThrows(IllegalStateException.class, () -> subtask.setId(100));
    }

    @Test
    public void testEqualityById(){
        Subtask s0 = new Subtask("Test 1", "Testing task 1", TaskStatus.NEW, 1);
        Subtask s1 = new Subtask("Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, 2);
        assertEquals(s0.getId(), s1.getId(), "tasks must have the same default id");
        assertEquals(s0, s1, "task entities should be compared by id");
    }

    @Test
    public void testNotSelfAttaching() {
        assertThrows(IllegalArgumentException.class, () -> new Subtask("Subtask 1", "Testing subtask 1", TaskStatus.NEW, 0));
    }
}
