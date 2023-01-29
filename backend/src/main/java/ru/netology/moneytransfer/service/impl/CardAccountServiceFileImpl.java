package ru.netology.moneytransfer.service.impl;

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

public class CardAccountServiceFileImpl extends CardAccountServiceImpl implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(CardAccountServiceFileImpl.class);

    private final Lock fileWriteLock = new ReentrantLock();
    private final ObjectMapper mapper;

    @Value("${cardaccounts.import.filepath:}")
    @Getter
    @Setter
    String importFilePath;
    @Value("${cardaccounts.export.filepath:}")
    @Getter
    @Setter
    String exportFilePath;

    public CardAccountServiceFileImpl(
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
            logger.debug("Загрузка CardAccount из {}", getImportFilePath());
            int count = importFromFile(importFilePath);
            logger.info("В репозиторий добавлены CardAccount: {}", count);
        }
    }

    public boolean isImportEnabled() {
        return importFilePath != null && !importFilePath.isBlank();
    }

    public boolean isExportEnabled() {
        return exportFilePath != null && !exportFilePath.isBlank();
    }

    public int importFromFile(String filePath) throws IOException {
        List<CardAccount> cardAccounts = mapper.readValue(new File(filePath), new TypeReference<>() {
        });
        cardAccountRepository.saveAll(cardAccounts);
        return cardAccounts.size();
    }

    public void exportToFile(String filePath) throws IOException {
        Iterable<CardAccount> accounts = cardAccountRepository.findAll();
        mapper.writeValue(new File(filePath), accounts);
    }

    @Override
    public CardAccount save(CardAccount cardAccount) {
        CardAccount savedCardAccount = super.save(cardAccount);
        tryExport();
        return savedCardAccount;
    }

    private void tryExport() {
        if (isExportEnabled() && fileWriteLock.tryLock()) {
            try {
                exportToFile(exportFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                fileWriteLock.unlock();
            }
        }
    }
}
