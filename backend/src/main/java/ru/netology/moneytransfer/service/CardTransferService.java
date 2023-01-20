package ru.netology.moneytransfer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import ru.netology.moneytransfer.bean.ConfirmationCodeGenerator;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.exception.NotFoundException;
import ru.netology.moneytransfer.exception.TransferException;
import ru.netology.moneytransfer.mapper.CardTransferOperationMapper;
import ru.netology.moneytransfer.model.request.CardTransferRequest;
import ru.netology.moneytransfer.model.response.OperationSuccess;
import ru.netology.moneytransfer.repository.CardTransferOperationRepository;
import ru.netology.moneytransfer.validation.CardTransferOperationValidator;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Validated
@Service
@RequiredArgsConstructor
public class CardTransferService {

    private final CardTransferOperationRepository cardTransferOperationRepository;
    private final CardTransferOperationMapper cardTransferOperationMapper;
    private final CardAccountService cardAccountService;
    private final ConfirmationCodeGenerator confirmationCodeGenerator;
    private final CardTransferOperationValidator cardTransferOperationValidator;

    @Value("${operation.fee.multiplier:0.0}")
    private BigDecimal feeMultiplier;

    public OperationSuccess makeTransferOperation(@Valid CardTransferRequest cardTransferRequest) {
        CardTransferOperation operation = cardTransferOperationMapper.fromRequest(cardTransferRequest);
        operation.setFee(calcFee(operation));
        operation.setConfirmationCode(confirmationCodeGenerator.get());

        validate(operation);
        operation = cardTransferOperationRepository.save(operation);
        return new OperationSuccess(operation.getId().toString());
    }

    public void validate(CardTransferOperation operation) {
        DataBinder dataBinder = new DataBinder(operation);
        dataBinder.setValidator(cardTransferOperationValidator);
        dataBinder.validate();
        BindingResult bindingResult = dataBinder.getBindingResult();
        if (bindingResult.hasErrors()) {
            throw new TransferException(operation, bindingResult.getFieldError().getDefaultMessage());
        }
    }

    private BigDecimal calcFee(CardTransferOperation operation) {
        return operation.getAmount().multiply(feeMultiplier);
    }

    public OperationSuccess confirmTransfer(String operationId, String confirmationCode) {
        UUID uid = UUID.fromString(operationId);

        CardTransferOperation operation = cardTransferOperationRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException(operationId, "Идентификатор перевода не найден."));

        if (operation.isConfirmed()) {
            throw new TransferException(operation, "Перевод уже был подтвержден.");
        }

        if (!operation.getConfirmationCode().equals(confirmationCode)) {
            throw new TransferException(operation, "Код подтверждения не совпадает.");
        }

        doTransferTransaction(operation);
        confirmOperationNow(operation);

        return new OperationSuccess(uid.toString());
    }

    private void doTransferTransaction(CardTransferOperation operation) {
        try {
            CardAccount cardFrom = cardAccountService.getAccountLocked(operation.getCardFromNumber())
                    .orElseThrow(() -> new TransferException(operation, "Ошибка получения карты отправителя."));

            CardAccount cardTo = cardAccountService.getAccountLocked(operation.getCardToNumber())
                    .orElseThrow(() -> new TransferException(operation, "Ошибка получения карты получателя."));

            BigDecimal amountWithFee = operation.getAmount().add(operation.getFee());

            if (!cardAccountService.checkAmount(cardFrom, amountWithFee)) {
                throw new TransferException(operation, "Недостаточно средств.");
            }

            cardFrom.setAmount(cardFrom.getAmount().subtract(amountWithFee));
            cardTo.setAmount(cardTo.getAmount().add(operation.getAmount()));

            cardAccountService.save(cardFrom);
            cardAccountService.save(cardTo);

        } catch (InterruptedException e) {
            throw new TransferException(operation, "Операция прервана.");
        } finally {
            cardAccountService.releaseLock(operation.getCardFromNumber());
            cardAccountService.releaseLock(operation.getCardToNumber());
        }
    }

    private void confirmOperationNow(CardTransferOperation operation) {
        operation.setConfirmed(true);
        operation.setConfirmedDateTime(LocalDateTime.now());
        cardTransferOperationRepository.save(operation);
    }

    public BigDecimal getFeeMultiplier() {
        return feeMultiplier;
    }

    public void setFeeMultiplier(BigDecimal feeMultiplier) {
        this.feeMultiplier = feeMultiplier;
    }
}
