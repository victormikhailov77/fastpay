package org.fastpay.entity;

import com.google.gson.JsonElement;
import lombok.Data;

@Data
public class ServiceResponse {
    private final TransferStatus status;
    private final String message;
    private final JsonElement data;

    public ServiceResponse(TransferStatus status, String message, JsonElement data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
