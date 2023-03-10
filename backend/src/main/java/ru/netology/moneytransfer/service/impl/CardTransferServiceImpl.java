package ru.netology.moneytransfer.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import ru.netology.moneytransfer.bean.ConfirmationCodeGenerator;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.exception.NotFoundException;
import ru.netology.moneytransfer.exception.TransferException;
import ru.netology.moneytransfer.mapper.CardTransferOperationMapper;
import ru.netology.moneytransfer.model.request.CardTransferRequest;
import ru.netology.moneytransfer.model.request.TransferConfirmationRequest;
import ru.netology.moneytransfer.model.response.OperationSuccess;
import ru.netology.moneytransfer.repository.CardTransferOperationRepository;
import ru.netology.moneytransfer.service.CardAccountService;
import ru.netology.moneytransfer.service.CardTransferService;
import ru.netology.moneytransfer.validation.CardTransferOperationValidator;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Validated
@Service
@RequiredArgsConstructor
public class CardTransferServiceImpl implements CardTransferService {

    private final CardTransferOperationRepository cardTransferOperationRepository;
    private final CardTransferOperationMapper cardTransferOperationMapper;
    private final CardAccountService cardAccountService;
    private final ConfirmationCodeGenerator confirmationCodeGenerator;
    private final CardTransferOperationValidator cardTransferOperationValidator;

    @Value("${operation.fee.multiplier:0.0}")
    @Getter
    @Setter
    private BigDecimal feeMultiplier;

    @Override public OperationSuccess registerTransfer(CardTransferRequest cardTransferRequest) {
        CardTransferOperation operation = cardTransferOperationMapper.fromRequest(cardTransferRequest);
        operation.setFee(calcFee(operation));
        operation.setConfirmationCode(confirmationCodeGenerator.get());

        cardTransferOperationValidator.validate(operation);
        operation = cardTransferOperationRepository.save(operation);
        return new OperationSuccess(operation.getId().toString());
    }

    private BigDecimal calcFee(CardTransferOperation operation) {
        return operation.getAmount().multiply(feeMultiplier);
    }

    @Override public OperationSuccess confirmTransfer(TransferConfirmationRequest confirmationRequest) {
        String operationId = confirmationRequest.getOperationId();
        UUID uid = UUID.fromString(operationId);

        CardTransferOperation operation = cardTransferOperationRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException(operationId, "?????????????????????????? ???????????????? ???? ????????????."));

        if (operation.isConfirmed()) {
            throw new TransferException(operation, "?????????????? ?????? ?????? ??????????????????????.");
        }

        if (!operation.getConfirmationCode().equals(confirmationRequest.getCode())) {
            throw new TransferException(operation, "?????? ?????????????????????????? ???? ??????????????????.");
        }

        executeTransfer(operation);
        confirmOperationNow(operation);

        return new OperationSuccess(uid.toString());
    }

    private void executeTransfer(CardTransferOperation operation) {
        CardAccount cardFrom = null;
        CardAccount cardTo = null;
        try {
            cardFrom = cardAccountService.getAccountLocked(operation.getCardFromNumber())
                    .orElseThrow(() -> new TransferException(operation, "???????? ?????????????????????? ????????????????????."));

            cardTo = cardAccountService.getAccountLocked(operation.getCardToNumber())
                    .orElseThrow(() -> new TransferException(operation, "???????? ???????????????????? ????????????????????."));

            BigDecimal amountWithFee = operation.getAmount().add(operation.getFee());

            if (!cardAccountService.checkAmount(cardFrom, amountWithFee)) {
                throw new TransferException(operation, "???????????????????????? ??????????????.");
            }

            cardFrom.setAmount(cardFrom.getAmount().subtract(amountWithFee));
            cardTo.setAmount(cardTo.getAmount().add(operation.getAmount()));

            cardAccountService.save(cardFrom);
            cardAccountService.save(cardTo);

        } catch (InterruptedException e) {
            throw new TransferException(operation, "???????????????? ????????????????.");
        } finally {
            if (cardFrom != null) cardAccountService.releaseLock(cardFrom.getCardNumber());
            if (cardTo != null) cardAccountService.releaseLock(cardTo.getCardNumber());
        }
    }

    private void confirmOperationNow(CardTransferOperation operation) {
        operation.setConfirmed(true);
        operation.setConfirmedDateTime(LocalDateTime.now());
        cardTransferOperationRepository.save(operation);
    }

}
