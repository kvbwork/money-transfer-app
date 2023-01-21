package ru.netology.moneytransfer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
public class TransferConfirmationRequest {
    @NotBlank(message = "Идентификатор операции не может быть пустым")
    @JsonProperty("operationId")
    String operationId;

    @NotBlank(message = "Код подтверждения не может быть пустым")
    @JsonProperty("code")
    String code;

}