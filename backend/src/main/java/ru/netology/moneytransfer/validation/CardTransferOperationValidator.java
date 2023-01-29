package ru.netology.moneytransfer.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.entity.CardTransferOperation;
import ru.netology.moneytransfer.service.CardAccountService;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CardTransferOperationValidator implements org.springframework.validation.Validator {

    @Getter
    @Setter
    @Value("${operation.currency.allowed:{}}")
    private Set<String> currencyAllowed;

    private final CardAccountService cardAccountService;

    @Override
    public boolean supports(Class<?> clazz) {
        return CardTransferOperation.class.isAssignableFrom(clazz);
    }

    public void validate(CardTransferOperation operation) {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(operation, "cardTransferOperation");
        validate(operation, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult.getFieldError().getDefaultMessage());
        }
    }

    @Override
    public void validate(Object target, Errors errors) {
        CardTransferOperation operation = (CardTransferOperation) target;
        validateBean(operation, errors);

        Optional<CardAccount> optFromAccount = cardAccountService.findByCardNumber(operation.getCardFromNumber());
        if (optFromAccount.isEmpty()) {
            errors.rejectValue("cardFromNumber", "", "Счет отправителя не найден.");
            return;
        }

        Optional<CardAccount> optToAccount = cardAccountService.findByCardNumber(operation.getCardToNumber());
        if (optToAccount.isEmpty()) {
            errors.rejectValue("cardToNumber", "", "Счет получателя не найден.");
            return;
        }

        validateWithAccount(operation, optFromAccount.get(), errors);
    }

    private void validateBean(CardTransferOperation operation, Errors errors) {
        if (operation.getCardFromNumber().equals(operation.getCardToNumber())) {
            errors.rejectValue("cardToNumber", "", "Отправитель и получатель не должны совпадать.");
        }

        if (!currencyAllowed.contains(operation.getCurrency())) {
            errors.rejectValue("currency", "", "Валюта перевода не поддерживается.");
        }
    }

    private void validateWithAccount(CardTransferOperation operation, CardAccount account, Errors errors) {
        if (!account.getValidTill().isEqual(operation.getCardFromValidTill())) {
            errors.rejectValue("cardFromValidTill", "", "Срок действия карты не совпадает.");
        }

        if (LocalDate.now().isAfter(account.getValidTill())) {
            errors.rejectValue("cardFromValidTill", "", "Срок действия карты истек.");
        }

        if (!account.getCvv().equals(operation.getCardFromCVV())) {
            errors.rejectValue("cardFromCVV", "", "Секретный код не совпадает.");
        }
    }

}
