package ru.netology.moneytransfer.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.keyvalue.annotation.KeySpace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;

@Data
@KeySpace("cardTransferOperation")
@NoArgsConstructor
@AllArgsConstructor
public class CardTransferOperation {

    @Id
    private UUID id;
    private LocalDateTime createdDateTime = LocalDateTime.now();

    private String cardFromNumber;
    private LocalDate cardFromValidTill;
    private String cardFromCVV;
    private String cardToNumber;

    private BigDecimal amount;
    private BigDecimal fee = ZERO;
    private String currency;

    private boolean confirmed;
    private String confirmationCode;
    private LocalDateTime confirmedDateTime;

}
