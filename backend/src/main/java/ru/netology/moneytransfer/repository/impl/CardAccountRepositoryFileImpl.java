package ru.netology.moneytransfer.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.repository.CardAccountRepository;

import java.util.Optional;

public class CardAccountRepositoryFileImpl extends AbstractRepositoryFileImpl<CardAccount, String> implements CardAccountRepository {
    private static String storageDir = "appdata/card-account";

    public CardAccountRepositoryFileImpl(ObjectMapper objectMapper) {
        super(storageDir, objectMapper, CardAccount.class);
    }

    @Override
    public Optional<CardAccount> findByCardNumber(String cardNumber) {
        return findById(cardNumber);
    }

    @Override
    protected String newId() {
        throw new IllegalStateException("CardAccount ID cannot be generated");
    }
}
