package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacourse.schedule.HttpTaskServer;
import ru.yandex.javacourse.schedule.http.adapter.DurationAdapter;
import ru.yandex.javacourse.schedule.http.adapter.LocalDateTimeAdapter;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;

public abstract class HttpTaskServerTest {
    protected TaskManager manager;
    protected HttpTaskServer taskServer;
    protected Gson gson;
    protected HttpClient client;

    @BeforeEach
    protected void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        client = HttpClient.newHttpClient();

        taskServer.start();
    }

    @AfterEach
    protected void shutDown() {
        taskServer.stop();
    }

    protected abstract URI takeURI();
}
