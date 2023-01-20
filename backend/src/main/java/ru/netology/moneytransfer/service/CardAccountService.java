package ru.netology.moneytransfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Service;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ConditionalOnSingleCandidate(CardAccountService.class)
@Service
@RequiredArgsConstructor
public class CardAccountService {
    protected final CardAccountRepository cardAccountRepository;
    private final CardAccountMapper cardAccountMapper;
    private final Map<String, Lock> lockMap = new ConcurrentHashMap<>();
    private int lockAwaitSeconds = 10;

    public Optional<CardAccount> findByCardNumber(String cardNumber) {
        return cardAccountRepository.findByCardNumber(cardNumber);
    }

    public CardAccount save(CardAccount cardAccount) {
        return cardAccountRepository.save(cardAccount);
    }

    public boolean checkAmount(CardAccount account, BigDecimal amount) {
        if (amount.signum() == -1)
            throw new IllegalArgumentException("Значение amount должно быть положительным.");
        return account.getLimit().compareTo(account.getAmount().subtract(amount)) <= 0;
    }

    public Optional<CardAccount> getAccountLocked(String cardNumber) throws InterruptedException {
        boolean successLock = lockMap.computeIfAbsent(cardNumber, k -> new ReentrantLock())
                .tryLock(getLockAwaitSeconds(), TimeUnit.SECONDS);
        if (successLock) {
            return cardAccountRepository.findByCardNumber(cardNumber)
                    .map(cardAccountMapper::makeCopy);
        }
        return Optional.empty();
    }

    public void releaseLock(String cardNumber) {
        Lock lock = lockMap.get(cardNumber);
        if (lock != null) {
            lock.unlock();
            lockMap.remove(cardNumber);
        }
    }

    public int getLockAwaitSeconds() {
        return lockAwaitSeconds;
    }

    public void setLockAwaitSeconds(int lockAwaitSeconds) {
        this.lockAwaitSeconds = lockAwaitSeconds;
    }
}
