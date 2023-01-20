package ru.netology.moneytransfer.mapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.netology.moneytransfer.model.request.CardTransferRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class CardTransferOperationMapperTest {

    static CardTransferRequest TEST_REQUEST;
    static CardTransferOperationMapper sut;

    @BeforeAll
    static void setup(){
        TEST_REQUEST = new CardTransferRequest(
                "1111222233334444", "12/99", "123",
                "4444333322221111",
                new CardTransferRequest.Amount(100_000L, "RUB")
        );
        sut = Mappers.getMapper(CardTransferOperationMapper.class);
    }

    @AfterAll
    static void afterAll() {
        sut = null;
    }

    @Test
    void fromRequest_success() {
        var expectedAmount = new BigDecimal(1000);
        var expectedDate = LocalDate.of(2099, 12, 31);
        var result = sut.fromRequest(TEST_REQUEST);

        assertThat(result.getCardFromNumber(), equalTo(TEST_REQUEST.getCardFromNumber()));
        assertThat(result.getCardFromValidTill(), equalTo(expectedDate));
        assertThat(result.getCardFromCVV(), equalTo(TEST_REQUEST.getCardFromCVV()));
        assertThat(result.getCardToNumber(), equalTo(TEST_REQUEST.getCardToNumber()));
        assertThat(result.getAmount().compareTo(expectedAmount), is(0));
        assertThat(result.getCurrency(), equalTo(TEST_REQUEST.getAmount().getCurrency()));
    }
}