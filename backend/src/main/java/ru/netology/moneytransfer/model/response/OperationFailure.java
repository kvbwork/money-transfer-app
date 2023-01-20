package ru.netology.moneytransfer.model.response;

import lombok.Value;

@Value
public class OperationFailure {
    int id;
    String message;
}
