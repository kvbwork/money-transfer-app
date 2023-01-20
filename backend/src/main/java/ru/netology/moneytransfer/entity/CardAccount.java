package ru.netology.moneytransfer.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.math.BigDecimal.ZERO;

@Data
@KeySpace("cardAccount")
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CardAccount {

    @Id
    private final String cardNumber;
    private LocalDate validTill;
    private String cvv;
    private BigDecimal amount = ZERO;
    private BigDecimal limit = ZERO;
    private String currency;

}
