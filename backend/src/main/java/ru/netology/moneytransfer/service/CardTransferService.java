package ru.netology.moneytransfer.service;

import ru.netology.moneytransfer.model.request.CardTransferRequest;
import ru.netology.moneytransfer.model.request.TransferConfirmationRequest;
import ru.netology.moneytransfer.model.response.OperationSuccess;

import javax.validation.Valid;

public interface CardTransferService {
    OperationSuccess registerTransfer(@Valid CardTransferRequest cardTransferRequest);

    OperationSuccess confirmTransfer(@Valid TransferConfirmationRequest confirmationRequest);
}
