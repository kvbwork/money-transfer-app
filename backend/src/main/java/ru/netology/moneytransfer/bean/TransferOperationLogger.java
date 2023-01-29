package ru.netology.moneytransfer.bean;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.netology.moneytransfer.entity.CardTransferOperation;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "operation.log.enabled", havingValue = "true")
public class TransferOperationLogger implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(TransferOperationLogger.class);

    @Getter
    @Setter
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
        String message = "Успешное завершение. " + operation;
        logger.info(message);
        writer.println(LocalDateTime.now() + " " + message);
        writer.flush();
    }

    public void logException(CardTransferOperation operation, Exception ex) {
        String message = "Ошибка! " + ex.getMessage() + operation;
        logger.info(message);
        writer.println(LocalDateTime.now() + " " + message);
        writer.flush();
    }

}
