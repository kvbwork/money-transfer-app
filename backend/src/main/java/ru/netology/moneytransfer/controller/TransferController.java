package ru.netology.moneytransfer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.moneytransfer.model.request.CardTransferRequest;
import ru.netology.moneytransfer.model.request.TransferConfirmationRequest;
import ru.netology.moneytransfer.model.response.OperationSuccess;
import ru.netology.moneytransfer.service.CardTransferService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class TransferController {

    private final CardTransferService cardTransferService;

    @PostMapping(path = "/transfer", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public OperationSuccess transfer(@RequestBody CardTransferRequest cardTransferRequest) {
        return cardTransferService.makeTransferOperation(cardTransferRequest);
    }

    @PostMapping(path = "/confirmOperation", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public OperationSuccess confirmOperation(@RequestBody TransferConfirmationRequest request) {
        return cardTransferService.confirmTransfer(request.getOperationId(), request.getCode());
    }

}
