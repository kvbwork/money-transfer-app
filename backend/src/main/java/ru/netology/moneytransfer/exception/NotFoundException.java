package ru.netology.moneytransfer.exception;

public class NotFoundException extends RuntimeException {

    private final String id;

    public NotFoundException(String id, String message) {
        super(message);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
