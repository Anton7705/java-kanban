package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.DisplayName;

@DisplayName("InMemoryTaskManager: Менеджер задач в оперативной памяти")
public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}
