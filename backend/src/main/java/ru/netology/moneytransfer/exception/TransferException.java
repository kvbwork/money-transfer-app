package ru.netology.moneytransfer.exception;

import ru.netology.moneytransfer.entity.CardTransferOperation;

public class TransferException extends RuntimeException {

    private final CardTransferOperation operation;

    public TransferException(CardTransferOperation operation, String message) {
        super(message);
        this.operation = operation;
    }

    public CardTransferOperation getOperation() {
        return operation;
    }
}
