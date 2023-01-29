package ru.netology.moneytransfer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;
import ru.netology.moneytransfer.service.impl.CardAccountServiceImpl;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CardAccountServiceImplTest {

    private static final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(1000L);

    CardAccount testDebitCardAccount;
    CardAccount testCreditCardAccount;
    CardAccountRepository cardAccountRepository;
    CardAccountMapper cardAccountMapper;
    CardAccountServiceImpl sut;

    @BeforeEach
    void setUp() {
        testDebitCardAccount = new CardAccount("1111", YearMonth.of(2099, 12).atEndOfMonth(), "111", TEST_AMOUNT, ZERO, "RUR");
        testCreditCardAccount = new CardAccount("2222", YearMonth.of(2099, 12).atEndOfMonth(), "222", ZERO, TEST_AMOUNT.negate(), "RUR");
        cardAccountRepository = Mockito.mock(CardAccountRepository.class);
        cardAccountMapper = Mappers.getMapper(CardAccountMapper.class);

        sut = new CardAccountServiceImpl(cardAccountRepository, cardAccountMapper);
        sut.setLockAwaitSeconds(1);

        Mockito.when(cardAccountRepository.findByCardNumber(testDebitCardAccount.getCardNumber()))
                .thenReturn(Optional.of(testDebitCardAccount));

        Mockito.when(cardAccountRepository.findByCardNumber(testCreditCardAccount.getCardNumber()))
                .thenReturn(Optional.of(testCreditCardAccount));
    }

    @AfterEach
    void tearDown() {
        sut = null;
        cardAccountMapper = null;
        cardAccountRepository = null;
    }

    @Test
    void checkAmount_negativeAmount_throwsException() {
        BigDecimal amount = TEST_AMOUNT.negate();
        assertThrowsExactly(IllegalArgumentException.class, () -> {
            sut.checkAmount(testDebitCardAccount, amount);
        });
    }

    @Test
    void checkAmount_DebitCard_success() {
        BigDecimal amount = TEST_AMOUNT;
        assertThat(sut.checkAmount(testDebitCardAccount, amount), is(true));
    }

    @Test
    void checkAmount_DebitCard_failed() {
        BigDecimal amount = TEST_AMOUNT.multiply(BigDecimal.valueOf(2));
        assertThat(sut.checkAmount(testDebitCardAccount, amount), is(false));
    }

    @Test
    void checkAmount_CreditCard_success() {
        BigDecimal amount = TEST_AMOUNT;
        assertThat(sut.checkAmount(testCreditCardAccount, amount), is(true));
    }

    @Test
    void checkAmount_CreditCard_failed() {
        BigDecimal amount = TEST_AMOUNT.multiply(BigDecimal.valueOf(2));
        assertThat(sut.checkAmount(testCreditCardAccount, amount), is(false));
    }

    @Test
    void getAccountLocked_success() throws InterruptedException {
        CardAccount account = sut.getAccountLocked(testDebitCardAccount.getCardNumber()).orElseThrow();
        assertThat(account, equalTo(testDebitCardAccount));
    }

    @Test
    void getAccountLocked_returnsCopy_success() throws InterruptedException {
        CardAccount accountEntity = testDebitCardAccount;
        CardAccount accountCopy = sut.getAccountLocked(accountEntity.getCardNumber()).orElseThrow();
        assertThat(accountEntity, equalTo(accountCopy));
        assertThat(accountEntity != accountCopy, is(true));
    }

    @Test
    void getAccountLocked_failure() throws InterruptedException {
        String cardNumber = testDebitCardAccount.getCardNumber();
        List<Optional<CardAccount>> accountHolder = new ArrayList<>(1);

        Thread thread = new Thread(() -> {
            try {
                accountHolder.add(sut.getAccountLocked(cardNumber));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        sut.getAccountLocked(cardNumber).orElseThrow();
        thread.start();
        thread.join();

        Optional<CardAccount> accountSecondReference = accountHolder.get(0);
        assertThat(accountSecondReference.isEmpty(), is(true));
    }

    @Test
    void releaseLock_success() throws InterruptedException {
        String cardNumber = testDebitCardAccount.getCardNumber();
        CardAccount accountFirstReference = sut.getAccountLocked(cardNumber).orElseThrow();
        sut.releaseLock(cardNumber);
        CardAccount accountSecondReference = sut.getAccountLocked(cardNumber).orElseThrow();
        assertThat(accountSecondReference, equalTo(testDebitCardAccount));
    }

    @Test
    void save_calls_respositorySave_success() {
        sut.save(testDebitCardAccount);
        verify(cardAccountRepository, times(1)).save(testDebitCardAccount);
    }
}