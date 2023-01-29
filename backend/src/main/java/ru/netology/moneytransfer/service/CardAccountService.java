package ru.netology.moneytransfer.service;

import ru.netology.moneytransfer.entity.CardAccount;

import java.math.BigDecimal;
import java.util.Optional;

public interface CardAccountService {
    Optional<CardAccount> findByCardNumber(String cardNumber);

    CardAccount save(CardAccount cardAccount);

    boolean checkAmount(CardAccount account, BigDecimal amount);

    Optional<CardAccount> getAccountLocked(String cardNumber) throws InterruptedException;

    void releaseLock(String cardNumber);
}
