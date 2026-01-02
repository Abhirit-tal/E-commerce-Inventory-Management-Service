package com.example.inventory.dto.sku;

import jakarta.validation.constraints.NotNull;

public class InventoryAdjustDTO {
    @NotNull
    private Integer delta;
    private String reason;
    private String idempotencyKey;

    public InventoryAdjustDTO() {
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
