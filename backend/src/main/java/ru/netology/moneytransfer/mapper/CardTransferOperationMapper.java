package ru.netology.moneytransfer.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.model.request.CardTransferRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface CardTransferOperationMapper {
    int AMOUNT_SCALE = 2;
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/uu");

    @Mapping(target = "cardFromValidTill", expression = "java( mapRequestTillDate(request) )")
    @Mapping(target = "amount", expression = "java( mapRequestAmount(request) )")
    @Mapping(target = "currency", source = "amount.currency")
    CardTransferOperation fromRequest(CardTransferRequest request);

    default BigDecimal mapRequestAmount(CardTransferRequest request) {
        return BigDecimal.valueOf(request.getAmount().getValue(), AMOUNT_SCALE);
    }

    default LocalDate mapRequestTillDate(CardTransferRequest request) {
        return YearMonth.parse(request.getCardFromValidTill(), DATE_FORMATTER).atEndOfMonth();
    }

}
