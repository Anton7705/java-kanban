package ru.yandex.javacourse.schedule.managerExceptions;

public class ManagerValidateException extends RuntimeException {
    public ManagerValidateException(String message) {
        super(message);
    }
}
