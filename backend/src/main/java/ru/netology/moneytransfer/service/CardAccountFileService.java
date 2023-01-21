package ru.netology.moneytransfer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Setter
public class CardAccountFileService extends CardAccountService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CardAccountFileService.class);

    private final Lock fileWriteLock = new ReentrantLock();
    private final ObjectMapper mapper;

    @Value("${cardaccounts.import.filepath:}")
    String importFilePath;
    @Value("${cardaccounts.export.filepath:}")
    String exportFilePath;

    public CardAccountFileService(
            CardAccountRepository cardAccountRepository,
            CardAccountMapper cardAccountMapper,
            ObjectMapper mapper
    ) {
        super(cardAccountRepository, cardAccountMapper);
        this.mapper = mapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isImportEnabled()) {
            importFromFile(importFilePath);
        }
    }

    public boolean isImportEnabled() {
        return importFilePath != null && !importFilePath.isBlank();
    }

    public boolean isExportEnabled() {
        return exportFilePath != null && !exportFilePath.isBlank();
    }

    public void importFromFile(String filePath) throws IOException {
        logger.debug("Загрузка CardAccount из {}", getImportFilePath());
        List<CardAccount> cardAccounts = mapper.readValue(new File(filePath), new TypeReference<>() {
        });
        cardAccountRepository.saveAll(cardAccounts);
        logger.info("В репозиторий добавлены CardAccount: {}", cardAccounts.size());
    }

    public void exportToFile(String filePath) throws IOException {
        Iterable<CardAccount> accounts = cardAccountRepository.findAll();
        mapper.writeValue(new File(filePath), accounts);
    }

    @Override
    public CardAccount save(CardAccount cardAccount) {
        CardAccount savedCardAccount = super.save(cardAccount);
        trySave();
        return savedCardAccount;
    }

    private void trySave() {
        if (isExportEnabled()) {
            boolean locked = fileWriteLock.tryLock();
            try {
                if (locked) {
                    exportToFile(exportFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (locked) fileWriteLock.unlock();
            }
        }
    }
}
