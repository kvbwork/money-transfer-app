package ru.netology.moneytransfer.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.DataBinder;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.service.CardAccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CardTransferOperationValidatorTest{

    static final LocalDateTime created = now();
    static final String cardNumber1 = "1111111111111111";
    static final String cardNumber2 = "2222222222222222";
    static final LocalDate validTill = LocalDate.of(2099, 12, 31);
    static final String cvv = "123";
    static final BigDecimal amount = BigDecimal.valueOf(1000);
    static final BigDecimal fee = new BigDecimal("10.00");
    static final String currency = "RUR";
    static final boolean confirmed = false;
    static final String code = "0000";

    static final LocalDate PAST_DATE = LocalDate.now().minusDays(1);

    CardAccountService cardAccountService;
    CardTransferOperationValidator sut;

    @BeforeEach
    void setUp(){
        cardAccountService = mock(CardAccountService.class);

        var card1 = new CardAccount(cardNumber1, validTill, cvv, BigDecimal.valueOf(10_000), ZERO, "RUR");
        var card2 = new CardAccount(cardNumber2, PAST_DATE, cvv, BigDecimal.valueOf(10_000), ZERO, "RUR");
        when(cardAccountService.findByCardNumber(cardNumber1)).thenReturn(Optional.of(card1));
        when(cardAccountService.findByCardNumber(cardNumber2)).thenReturn(Optional.of(card2));

        sut = new CardTransferOperationValidator(cardAccountService);
        sut.setCurrencyAllowed(Set.of(currency));
    }

    @Test
    void valid_success() {
        var operation = new CardTransferOperation(null, created, cardNumber1, validTill, cvv, cardNumber2,
                amount, fee, currency, confirmed, code, now());
        DataBinder dataBinder = new DataBinder(operation);
        dataBinder.setValidator(sut);
        dataBinder.validate();
        System.out.println(dataBinder.getBindingResult());
        assertThat(dataBinder.getBindingResult().hasErrors(), is(false));
    }

    @ParameterizedTest
    @MethodSource("operationIncorrectParamsSource")
    void incorrect_params_failure(CardTransferOperation operation, Object actual) {
        DataBinder dataBinder = new DataBinder(operation);
        dataBinder.setValidator(sut);
        dataBinder.validate();
        var error = dataBinder.getBindingResult().getFieldError();
        System.out.println(dataBinder.getBindingResult());
        System.out.println(error.getDefaultMessage());
        assertThat(error.getRejectedValue(), equalTo(actual));
    }

    public static Stream<Arguments> operationIncorrectParamsSource() {
        return Stream.of(
                of(new CardTransferOperation(null, created, "999", validTill, cvv, cardNumber2, amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, cardNumber1, validTill, cvv, "999", amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, cardNumber1, validTill, cvv, cardNumber1, amount, fee, currency, confirmed, code, now()), cardNumber1),
                of(new CardTransferOperation(null, created, cardNumber2, PAST_DATE, cvv, cardNumber1, amount, fee, currency, confirmed, code, now()), PAST_DATE),
                of(new CardTransferOperation(null, created, cardNumber1, validTill, "999", cardNumber2, amount, fee, currency, confirmed, code, now()), "999"),
                of(new CardTransferOperation(null, created, cardNumber1, validTill, cvv, cardNumber2, amount, fee, "ZZZ", confirmed, code, now()), "ZZZ")
        );
    }

}