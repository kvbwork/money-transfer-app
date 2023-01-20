package ru.netology.moneytransfer.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class TransferConfirmationRequest {
    @JsonProperty("operationId")
    String operationId;

    @JsonProperty("code")
    String code;

}