package ru.netology.moneytransfer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import ru.netology.moneytransfer.validation.CardTransferOperationValidator;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static java.math.BigDecimal.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardTransferServiceTest {

    static final BigDecimal TEST_AMOUNT_VALUE = new BigDecimal("1000");
    static final BigDecimal TEST_FEE_MULTIPLIER = new BigDecimal("0.01");
    static final BigDecimal TEST_FEE_AMOUNT = new BigDecimal("10.00");
    static final String TEST_OPERATION_ID = "8914c1e4-6726-486c-8381-2bfe0fb36ae3";
    static final String TEST_CONFIRM__CODE = "1234";
    static final CardTransferRequest TEST_REQUEST = new CardTransferRequest(
            "1111222233334444", "12/99", "123",
            "4444333322221111",
            new CardTransferRequest.Amount(100_000L, "RUB")
    );
    static final TransferConfirmationRequest TEST_CONFIRMATION_REQUEST = new TransferConfirmationRequest(
            TEST_OPERATION_ID, TEST_CONFIRM__CODE
    );

    CardTransferOperationRepository cardTransferOperationRepository;
    CardTransferOperationMapper cardTransferOperationMapper;
    CardAccountService cardAccountService;
    ConfirmationCodeGenerator confirmationCodeGenerator;
    CardTransferOperationValidator cardTransferOperationValidator;

    CardTransferOperation operation;
    CardAccount cardAccountFrom;
    CardAccount cardAccountTo;

    CardTransferService sut;

    @BeforeEach
    void setUp() throws InterruptedException {
        cardTransferOperationValidator = mock(CardTransferOperationValidator.class);
        confirmationCodeGenerator = mock(ConfirmationCodeGenerator.class);
        cardTransferOperationMapper = mock(CardTransferOperationMapper.class);
        cardTransferOperationRepository = mock(CardTransferOperationRepository.class);
        cardAccountService = mock(CardAccountService.class);
        operation = mock(CardTransferOperation.class);
        cardAccountFrom = mock(CardAccount.class);
        cardAccountTo = mock(CardAccount.class);

        when(cardTransferOperationValidator.supports(any())).thenReturn(true);

        when(confirmationCodeGenerator.get()).thenReturn(TEST_CONFIRM__CODE);
        when(cardTransferOperationMapper.fromRequest(any())).thenReturn(operation);

        when(cardTransferOperationRepository.save(operation)).thenReturn(operation);
        when(cardTransferOperationRepository.findById(UUID.fromString(TEST_OPERATION_ID))).thenReturn(Optional.of(operation));

        when(cardAccountService.getAccountLocked(TEST_REQUEST.getCardFromNumber())).thenReturn(Optional.of(cardAccountFrom));
        when(cardAccountService.getAccountLocked(TEST_REQUEST.getCardToNumber())).thenReturn(Optional.of(cardAccountTo));
        when(cardAccountService.checkAmount(any(CardAccount.class), any(BigDecimal.class))).thenReturn(true);

        when(operation.getId()).thenReturn(UUID.fromString(TEST_OPERATION_ID));
        when(operation.getCardFromNumber()).thenReturn(TEST_REQUEST.getCardFromNumber());
        when(operation.getCardToNumber()).thenReturn(TEST_REQUEST.getCardToNumber());
        when(operation.getAmount()).thenReturn(TEST_AMOUNT_VALUE);
        when(operation.getFee()).thenReturn(TEST_FEE_AMOUNT);
        when(operation.getConfirmationCode()).thenReturn(TEST_CONFIRM__CODE);

        when(cardAccountFrom.getAmount()).thenReturn(ZERO);
        when(cardAccountTo.getAmount()).thenReturn(ZERO);

        sut = new CardTransferService(
                cardTransferOperationRepository,
                cardTransferOperationMapper,
                cardAccountService,
                confirmationCodeGenerator,
                cardTransferOperationValidator
        );
        sut.setFeeMultiplier(TEST_FEE_MULTIPLIER);
    }

    @Test
    void makeTransferOperation_call_Mapper() {
        sut.makeTransferOperation(TEST_REQUEST);
        verify(cardTransferOperationMapper, times(1)).fromRequest(any(CardTransferRequest.class));
    }

    @Test
    void makeTransferOperation_save_operation() {
        sut.makeTransferOperation(TEST_REQUEST);
        verify(cardTransferOperationRepository, times(1)).save(operation);
    }

    @Test
    void makeTransferOperation_return_Id() {
        OperationSuccess result = sut.makeTransferOperation(TEST_REQUEST);
        assertThat(result.getOperationId(), equalTo(TEST_OPERATION_ID));
    }

    @Test
    void makeTransferOperation_setFee() {
        OperationSuccess result = sut.makeTransferOperation(TEST_REQUEST);
        verify(operation, times(1)).setFee(TEST_FEE_AMOUNT);
    }

    @Test
    void confirmTransfer_setNewAmountValues() {
        var amountWithFee = operation.getAmount().add(operation.getFee());
        var expectedCardFromAmount = cardAccountFrom.getAmount().subtract(amountWithFee);
        var expectedCardToAmount = cardAccountTo.getAmount().add(operation.getAmount());

        sut.confirmTransfer(TEST_CONFIRMATION_REQUEST);
        verify(cardAccountFrom, times(1)).setAmount(expectedCardFromAmount);
        verify(cardAccountTo, times(1)).setAmount(expectedCardToAmount);
    }

    @Test
    void confirmTransfer_saveConfirmedOperation_success() {
        sut.confirmTransfer(TEST_CONFIRMATION_REQUEST);
        verify(operation, times(1)).setConfirmed(true);
        verify(cardTransferOperationRepository, times(1)).save(operation);
    }

    @Test
    void confirmTransfer_wrongConfirmationCode_throwsException() {
        var wrongConfirmCode = "7777";
        assertThrowsExactly(TransferException.class, () -> {
            sut.confirmTransfer(new TransferConfirmationRequest(TEST_OPERATION_ID, wrongConfirmCode));
        });
    }

    @Test
    void confirmTransfer_alreadyConfirmed_throwsException() {
        when(operation.isConfirmed()).thenReturn(true);
        assertThrowsExactly(TransferException.class, () -> {
            sut.confirmTransfer(TEST_CONFIRMATION_REQUEST);
        });
    }

    @Test
    void confirmTransfer_unknownId_throwsException() {
        assertThrowsExactly(NotFoundException.class, () -> {
            sut.confirmTransfer(new TransferConfirmationRequest(UUID.randomUUID().toString(), TEST_OPERATION_ID));
        });
    }

}