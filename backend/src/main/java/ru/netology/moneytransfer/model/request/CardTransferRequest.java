package ru.netology.moneytransfer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;

@Value
public class CardTransferRequest {
    @Pattern(regexp = "[\\d]{16,19}", message = "Номер карты отправителя задан неверно.")
    @JsonProperty("cardFromNumber")
    String cardFromNumber;

    @Pattern(regexp = "[\\d]{2}/[\\d]{2}", message = "Формат даты ММ/ГГ задан неверно.")
    @JsonProperty("cardFromValidTill")
    String cardFromValidTill;

    @Pattern(regexp = "[\\d]{3}", message = "Проверочный код должен состоять из 3-х цифр.")
    @JsonProperty("cardFromCVV")
    String cardFromCVV;

    @Pattern(regexp = "[\\d]{16,19}", message = "Номер карты получателя задан неверно.")
    @JsonProperty("cardToNumber")
    String cardToNumber;

    @Valid
    @JsonProperty("amount")
    Amount amount;

    @Value
    public static class Amount {
        @JsonProperty("value")
        @Positive(message = "Сумма должна быть положительной.")
        long value;

        @NotBlank(message = "Не задан код валюты для операции.")
        @JsonProperty("currency")
        String currency;
    }

}