package ru.netology.moneytransfer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;
import ru.netology.moneytransfer.repository.CardTransferOperationRepository;
import ru.netology.moneytransfer.repository.impl.CardAccountRepositoryFileImpl;
import ru.netology.moneytransfer.repository.impl.CardTransferOperationRepositoryFileImpl;
import ru.netology.moneytransfer.service.impl.CardAccountServiceFileImpl;

@Configuration
@EnableAspectJAutoProxy
public class MainConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Bean
    public CardAccountServiceFileImpl cardAccountService(
            CardAccountRepository cardAccountRepository,
            CardAccountMapper cardAccountMapper,
            ObjectMapper objectMapper
    ) {
        return new CardAccountServiceFileImpl(cardAccountRepository, cardAccountMapper, objectMapper);
    }

    @Bean
    public CardAccountRepository cardAccountRepository(ObjectMapper objectMapper) {
        return new CardAccountRepositoryFileImpl(objectMapper);
    }

    @Bean
    public CardTransferOperationRepository cardTransferOperationRepository(ObjectMapper objectMapper) {
        return new CardTransferOperationRepositoryFileImpl(objectMapper);
    }

}
