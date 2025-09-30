package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    @Test
    public void testIdIsImmutable(){
        Epic epic = new Epic("Epic 1", "Testing epic 1");
        epic.isManagedTrue();
        assertThrows(IllegalStateException.class, () -> epic.setId(100));
    }

    @Test
    public void testSubtaskUniqueIds() {
        Epic epic = new Epic("Epic 1", "Testing epic 1");
        epic.addSubtaskId(1);
        epic.addSubtaskId(2);
        assertEquals(2, epic.subtaskIds.size(), "should add distinct subtask ids");
        epic.addSubtaskId(1);
        assertEquals(2, epic.subtaskIds.size(), "should not add same subtask id twice");
    }

    @Test
    public void testNotSelfAttaching() {
        int id = 1;
        Epic epic = new Epic( "Epic 1", "Testing epic 1");
        epic.setId(id);
        assertThrows(IllegalArgumentException.class, () -> epic.addSubtaskId(id));
        assertEquals(0, epic.subtaskIds.size(), "epic should not add itself as subtask");
    }
}
