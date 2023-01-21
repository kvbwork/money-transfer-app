package ru.netology.moneytransfer.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.netology.moneytransfer.entity.CardTransferOperation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "operation.log.enabled", havingValue = "true")
public class TransferOperationLogger implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(TransferOperationLogger.class);

    @Value("${operation.log.filepath}")
    private String logFileName;

    private PrintWriter writer;

    @Override
    public void afterPropertiesSet() throws Exception {
        writer = new PrintWriter(new FileOutputStream(logFileName, true));
    }

    @Override
    public void destroy() throws Exception {
        writer.flush();
        writer.close();
    }

    public void logSuccess(CardTransferOperation operation) {
        String message = String.format("Успешный перевод %s от %s со счета %s на счет %s, сумма: %.2f %s, комиссия: %.2f %s. Подтверждено кодом: %s в %s",
                operation.getId(),
                operation.getCreatedDateTime(),
                operation.getCardFromNumber(),
                operation.getCardToNumber(),
                operation.getAmount(), operation.getCurrency(),
                operation.getFee(), operation.getCurrency(),
                operation.getConfirmationCode(),
                operation.getConfirmedDateTime()
        );
        logger.info(message);
        writer.println(LocalDateTime.now() + " " + message);
        writer.flush();
    }

    public void logException(CardTransferOperation operation, Exception ex) {
        String message = String.format("Ошибка! %s Перевод %s от %s со счета %s на счет %s, сумма: %.2f %s, комиссия: %.2f %s. Код подтверждения: %s (%s)",
                ex.getMessage(),
                operation.getId(),
                operation.getCreatedDateTime(),
                operation.getCardFromNumber(),
                operation.getCardToNumber(),
                operation.getAmount(), operation.getCurrency(),
                operation.getFee(), operation.getCurrency(),
                operation.getConfirmationCode(),
                operation.getConfirmedDateTime()
        );
        logger.info(message);
        writer.println(LocalDateTime.now() + " " + message);
        writer.flush();
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }
}
