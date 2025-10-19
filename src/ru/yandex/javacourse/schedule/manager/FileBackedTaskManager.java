package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path filePath;
    private static final String HEAD = "id,type,name,status,description,epic/subtaskIds";

    public FileBackedTaskManager(Path filePath) {
        this.filePath = filePath;
        fileExists();
    }
    @Override
    public int addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }


    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    public static FileBackedTaskManager loadFromFile(File src) {
        Path path = src.toPath();
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        if (!Files.exists(path)) {
            throw new ManagerSaveException("File does not exist");
        }
        try (BufferedReader bufferedReader = Files.newBufferedReader(path, StandardCharsets.UTF_8)){
            String header = bufferedReader.readLine();
            if (!header.equals(HEAD)) {
                throw new ManagerSaveException("Invalid file header. Expected: " + HEAD + ", but was: " + header);
            }
            int maxId = 0;
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                Task parsed = fromString(line);

                if (parsed instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic);
                } else if (parsed instanceof Subtask subtask) {
                    manager.subtasks.put(subtask.getId(), subtask);
                } else if (parsed instanceof Task task){
                    manager.tasks.put(task.getId(), task);
                }
            }
            manager.generatorId = maxId;
            return manager;
        } catch (IOException e) {
            throw new ManagerSaveException("Error reading file", e);
        }
    }

    private void save() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            bufferedWriter.write(HEAD);
            bufferedWriter.newLine();

            for (Task task : getTasks()) {
                bufferedWriter.write(task.toStringForFile());
                bufferedWriter.newLine();
            }
            for (Epic epic : getEpics()) {
                bufferedWriter.write(epic.toStringForFile());
                bufferedWriter.newLine();
            }
            for (Subtask subtask : getSubtasks()) {
                bufferedWriter.write(subtask.toStringForFile());
                bufferedWriter.newLine();
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Error saving to file", ex);
        }
    }

    private void fileExists() {
        try {
            if (!Files.exists(filePath)) {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.createFile(filePath);
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Error creating file", ex);
        }
    }

    private static Task fromString(String value) {
        String[] fieldes = value.split(",");
        int id = Integer.parseInt(fieldes[0]);
        TaskType taskType = TaskType.valueOf(fieldes[1]);
        String name = fieldes[2];
        TaskStatus status = TaskStatus.valueOf(fieldes[3]);
        String description = fieldes[4];
        switch (taskType) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(fieldes[5]);
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                return subtask;
            }
            case EPIC -> {
                String firstSetElement = fieldes[5].replace("[", "");
                fieldes[5] = firstSetElement;
                String lastSetElement = fieldes[fieldes.length - 1].replace("]", "");
                fieldes[fieldes.length - 1] = lastSetElement;
                Epic epic = new Epic(name, description);
                epic.setStatus(status);
                epic.setId(id);
                if (fieldes[5].isEmpty()) {
                    return epic;
                }
                for (int i = 5; i < fieldes.length; i++) {
                    epic.addSubtaskId(Integer.parseInt(fieldes[i].trim()));
                }
                return epic;
            }
            default -> throw new IllegalArgumentException("This string is not intended for parsing");
        }
    }
}
