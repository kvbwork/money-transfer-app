package ru.netology.moneytransfer.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;
import ru.netology.moneytransfer.service.CardAccountService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class CardAccountServiceImpl implements CardAccountService {
    protected final CardAccountRepository cardAccountRepository;
    private final CardAccountMapper cardAccountMapper;
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int lockAwaitSeconds = 10;

    @Override
    public Optional<CardAccount> findByCardNumber(String cardNumber) {
        return cardAccountRepository.findByCardNumber(cardNumber)
                .map(cardAccountMapper::makeCopy);
    }

    @Override
    public CardAccount save(CardAccount cardAccount) {
        return cardAccountRepository.save(cardAccount);
    }

    @Override
    public boolean checkAmount(CardAccount account, BigDecimal amount) {
        if (amount.signum() == -1)
            throw new IllegalArgumentException("Значение amount должно быть положительным.");
        return account.getLimit().compareTo(account.getAmount().subtract(amount)) <= 0;
    }

    @Override
    public Optional<CardAccount> getAccountLocked(String cardNumber) throws InterruptedException {
        boolean successLock = lockMap.computeIfAbsent(cardNumber, k -> new ReentrantLock())
                .tryLock(getLockAwaitSeconds(), TimeUnit.SECONDS);
        if (successLock) {
            return findByCardNumber(cardNumber);
        }
        return Optional.empty();
    }

    @Override
    public void releaseLock(String cardNumber) {
        Lock lock = lockMap.get(cardNumber);
        if (lock != null) {
            lock.unlock();
            lockMap.remove(cardNumber);
        }
    }

}
