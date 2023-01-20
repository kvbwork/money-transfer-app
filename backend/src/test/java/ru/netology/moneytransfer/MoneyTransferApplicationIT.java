package ru.netology.moneytransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoneyTransferApplicationIT {

    static final String TRANSFER_REQUEST_JSON =
            "{\"cardFromNumber\": \"1111111111111111\"," +
                    "\"cardToNumber\": \"2222222222222222\"," +
                    "\"cardFromCVV\": \"111\"," +
                    "\"cardFromValidTill\": \"12/99\"," +
                    "\"amount\": { \"currency\": \"RUR\", \"value\": 200000}}";

    static final String DOCKER_IMAGE_NAME = "kvbdev/money-transfer-rest:latest";
    static final int APP_PORT = 5500;

    static final GenericContainer<?> DEV_APP = new GenericContainer<>(DOCKER_IMAGE_NAME)
            .withExposedPorts(APP_PORT)
            .withFileSystemBind("./appdata", "/appdata");

    static String appUrl;
    static String transferUrl;
    static String confirmOperationUrl;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeAll
    public static void setUp() {
        DEV_APP.start();
        appUrl = String.format("http://%s:%d", DEV_APP.getHost(), DEV_APP.getMappedPort(APP_PORT));
        transferUrl = appUrl + "/transfer";
        confirmOperationUrl = appUrl + "/confirmOperation";
    }

    @Test
    void contextLoads() {
        System.out.println("INTEGRATION TEST");
    }

    @Test
    void post_transfer_success() throws Exception {
        executeTransferRequest(TRANSFER_REQUEST_JSON);
    }

    String executeTransferRequest(String jsonBody) throws Exception {
        var response = postJson(transferUrl, jsonBody);
        var statusCode = response.getStatusCodeValue();
        var contentType = response.getHeaders().getContentType();
        var operationId = parseJsonMap(response.getBody()).get("operationId");

        assertThat(statusCode, is(200));
        assertThat(contentType, equalTo(APPLICATION_JSON));
        assertThat(operationId.isBlank(), is(false));

        return operationId;
    }

    ResponseEntity<String> postJson(String url, String jsonBody) {
        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        var request = new HttpEntity<>(jsonBody, headers);
        return restTemplate.postForEntity(url, request, String.class);
    }

    Map<String, String> parseJsonMap(String content) throws JsonProcessingException {
        return objectMapper.readValue(content,
                new TypeReference<HashMap<String, String>>() {
                });
    }

    @Test
    void post_transfer_then_confirm_success() throws Exception {
        var transferOperationId = executeTransferRequest(TRANSFER_REQUEST_JSON);
        var confirmOperationId = executeConfirmRequest(transferOperationId);
        assertEquals(transferOperationId, confirmOperationId);
    }

    String executeConfirmRequest(String transferOperationId) throws Exception {
        var confirmOperationJson = String.format("{\"code\": \"%s\", \"operationId\": \"%s\"}",
                "0000", transferOperationId);

        var response = postJson(confirmOperationUrl, confirmOperationJson);
        var statusCode = response.getStatusCodeValue();
        var contentType = response.getHeaders().getContentType();
        var operationId = parseJsonMap(response.getBody()).get("operationId");

        assertThat(statusCode, is(200));
        assertThat(contentType, equalTo(APPLICATION_JSON));
        assertThat(operationId.isBlank(), is(false));

        return operationId;
    }

}
