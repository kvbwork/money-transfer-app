package ru.netology.moneytransfer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.netology.moneytransfer.entity.CardAccount;
import ru.netology.moneytransfer.repository.CardAccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {MoneyTransferApplication.class})
class MoneyTransferApplicationTests {

    static final String TRANSFER_PATH = "/transfer";
    static final String CONFIRM_OPERATION_PATH = "/confirmOperation";

    static final String TRANSFER_REQUEST_JSON =
            "{\"cardFromNumber\": \"1111111111111111\"," +
                    "\"cardToNumber\": \"2222222222222222\"," +
                    "\"cardFromCVV\": \"111\"," +
                    "\"cardFromValidTill\": \"12/99\"," +
                    "\"amount\": { \"currency\": \"RUR\", \"value\": 200000}}";

    static final LocalDate TEST_TILL_VALID_DATE = LocalDate.of(2099,12,31);
    static final BigDecimal DEFAULT_TEST_ACCOUNT_AMOUNT = BigDecimal.valueOf(10000L);
    static final BigDecimal TRANSFER_AMOUNT = BigDecimal.valueOf(2000L);
    static final BigDecimal FEE_AMOUNT = BigDecimal.valueOf(20L);

    @Autowired
    WebApplicationContext context;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    CardAccountRepository cardAccountRepository;

    MockMvc mockMvc;
    CardAccount card1;
    CardAccount card2;

    @BeforeEach
    void setUp() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(context);
        mockMvc = builder.build();

        card1 = new CardAccount("1111111111111111", TEST_TILL_VALID_DATE, "111",
                DEFAULT_TEST_ACCOUNT_AMOUNT, BigDecimal.ZERO, "RUR");
        card2 = new CardAccount("2222222222222222", TEST_TILL_VALID_DATE, "222",
                DEFAULT_TEST_ACCOUNT_AMOUNT, BigDecimal.ZERO, "RUR");

        cardAccountRepository.save(card1);
        cardAccountRepository.save(card2);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void post_transfer_success() throws Exception {
        executeTransferRequest(TRANSFER_REQUEST_JSON);
    }

    String executeTransferRequest(String jsonBody) throws Exception {
        var response = mockMvc.perform(post(TRANSFER_PATH)
                .contentType(APPLICATION_JSON)
                .content(jsonBody)
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON)
        ).andReturn().getResponse();

        var responseMap = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<HashMap<String, String>>() {
                });

        return responseMap.get("operationId");
    }

    @Test
    void post_transfer_then_confirm_success() throws Exception {
        var transferOperationId = executeTransferRequest(TRANSFER_REQUEST_JSON);
        var confirmOperationId = executeConfirmRequest(transferOperationId);
        assertEquals(transferOperationId, confirmOperationId);
    }

    String executeConfirmRequest(String operationId) throws Exception {
        var confirmOperationJson = String.format("{\"code\": \"%s\", \"operationId\": \"%s\"}",
                "0000", operationId);

        var response = mockMvc.perform(post(CONFIRM_OPERATION_PATH)
                .contentType(APPLICATION_JSON)
                .content(confirmOperationJson)
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON)
        ).andReturn().getResponse();

        var responseMap = objectMapper.readValue(response.getContentAsString(),
                new TypeReference<HashMap<String, String>>() {
                });

        return responseMap.get("operationId");
    }

    @Test
    void testTransferAmount() throws Exception {
        var transferOperationId = executeTransferRequest(TRANSFER_REQUEST_JSON);
        var confirmOperationId = executeConfirmRequest(transferOperationId);

        var sourceCard = cardAccountRepository.findByCardNumber(card1.getCardNumber()).orElseThrow();
        var targetCard = cardAccountRepository.findByCardNumber(card2.getCardNumber()).orElseThrow();

        var sourceCardExpectedAmount = DEFAULT_TEST_ACCOUNT_AMOUNT.subtract(TRANSFER_AMOUNT).subtract(FEE_AMOUNT);
        var targetCardExpectedAmount = DEFAULT_TEST_ACCOUNT_AMOUNT.add(TRANSFER_AMOUNT);

        assertEquals(0, sourceCard.getAmount().compareTo(sourceCardExpectedAmount));
        assertEquals(0, targetCard.getAmount().compareTo(targetCardExpectedAmount));
    }

}
