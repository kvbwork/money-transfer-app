package ru.netology.moneytransfer.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.repository.CardTransferOperationRepository;

import java.util.UUID;

public class CardTransferOperationRepositoryFileImpl extends AbstractRepositoryFileImpl<CardTransferOperation, UUID> implements CardTransferOperationRepository {
    private static String storageDir = "appdata/card-transfer-operation";

    public CardTransferOperationRepositoryFileImpl(ObjectMapper objectMapper) {
        super(storageDir, objectMapper, CardTransferOperation.class);
    }

    @Override
    protected UUID newId() {
        return UUID.randomUUID();
    }
}
