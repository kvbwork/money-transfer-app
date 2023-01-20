package ru.netology.moneytransfer.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.DataBinder;
import ru.netology.moneytransfer.MoneyTransferApplication;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.repository.CardAccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.of;

@SpringBootTest(classes = {MoneyTransferApplication.class})
class CardTransferOperationValidatorTest implements InitializingBean {

    static final LocalDateTime created = now();
    static final String card1 = "1111111111111111";
    static final String card2 = "2222222222222222";
    static final LocalDate validTill = LocalDate.of(2099, 12, 31);
    static final String cvv = "123";
    static final BigDecimal amount = BigDecimal.valueOf(1000);
    static final BigDecimal fee = new BigDecimal("10.00");
    static final String currency = "RUR";
    static final boolean confirmed = false;
    static final String code = "0000";

    static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    @Autowired
    CardTransferOperationValidator cardTransferOperationValidator;

    @Autowired
    CardAccountRepository cardAccountRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        cardAccountRepository.save(new CardAccount(card1, validTill, cvv, BigDecimal.valueOf(10_000), ZERO, "RUR"));
        cardAccountRepository.save(new CardAccount(card2, PAST_DATE, cvv, BigDecimal.valueOf(10_000), ZERO, "RUR"));
    }

    @Test
    void valid_success() {
        var operation = new CardTransferOperation(null, created, card1, validTill, cvv, card2,
                amount, fee, currency, confirmed, code, now());
        DataBinder dataBinder = new DataBinder(operation);
        dataBinder.setValidator(cardTransferOperationValidator);
        dataBinder.validate();
        System.out.println(dataBinder.getBindingResult());
        assertThat(dataBinder.getBindingResult().hasErrors(), is(false));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @MethodSource("incorrectRequestParamsSource")
    void incorrect_params_failure(CardTransferOperation operation, Object actual) {
        DataBinder dataBinder = new DataBinder(operation);
        dataBinder.setValidator(cardTransferOperationValidator);
        dataBinder.validate();
        var error = dataBinder.getBindingResult().getFieldError();
        System.out.println(dataBinder.getBindingResult());
        System.out.println(error.getDefaultMessage());
        assertThat(error.getRejectedValue(), equalTo(actual));
    }

    public static Stream<Arguments> incorrectRequestParamsSource() {
        return Stream.of(
                of(new CardTransferOperation(null, created, "999", validTill, cvv, card2, amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, card1, validTill, cvv, "999", amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, card1, validTill, cvv, card1, amount, fee, currency, confirmed, code, now()), card1),
                of(new CardTransferOperation(null, created, card2, PAST_DATE, cvv, card1, amount, fee, currency, confirmed, code, now()), PAST_DATE),
                of(new CardTransferOperation(null, created, card1, validTill, "999", card2, amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, card1, validTill, cvv, card2, amount, fee, "ZZZ", confirmed, code, now()), "ZZZ")
        );
    }


}