package ru.netology.moneytransfer.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.netology.moneytransfer.bean.TransferOperationLogger;
import ru.netology.moneytransfer.exception.TransferException;
import ru.netology.moneytransfer.repository.CardTransferOperationRepository;

import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "operation.log.enabled", havingValue = "true")
public class CardTransferServiceAuditing {

    private final CardTransferOperationRepository cardTransferOperationRepository;
    private final TransferOperationLogger transferOperationLogger;

    @AfterReturning("execution(* *..CardTransferService.confirmTransfer(String,String))")
    public void afterReturningConfirmTransfer(JoinPoint point) {
        UUID operationId = UUID.fromString((String) point.getArgs()[0]);
        cardTransferOperationRepository.findById(operationId)
                .ifPresent(transferOperationLogger::logSuccess);
    }

    @AfterThrowing(pointcut = "execution(* *..CardTransferService.confirmTransfer(String,String))", throwing = "ex")
    public void afterThrowingConfirmTransfer(JoinPoint point, TransferException ex) {
        transferOperationLogger.logException(ex.getOperation(), ex);
    }

}
