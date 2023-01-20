package ru.netology.moneytransfer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.netology.moneytransfer.mapper.CardAccountMapper;
import ru.netology.moneytransfer.repository.CardAccountRepository;
import ru.netology.moneytransfer.service.CardAccountFileService;
import ru.netology.moneytransfer.service.CardAccountService;

@Configuration
@EnableAspectJAutoProxy
@EnableMapRepositories("ru.netology.moneytransfer.repository")
public class MainConfig implements WebMvcConfigurer {

    @Autowired
    ApplicationContext context;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

    @Bean
    public CardAccountService cardAccountFileService(
            CardAccountRepository cardAccountRepository,
            CardAccountMapper cardAccountMapper,
            ObjectMapper objectMapper
    ) {
        return new CardAccountFileService(cardAccountRepository, cardAccountMapper, objectMapper);
    }

}
