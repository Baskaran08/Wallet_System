package com.fintech.Dto;

import java.math.BigDecimal;

public class TransferRequest {
    private Long receiverId;
    private BigDecimal amount;

    public TransferRequest() {
    }

    public TransferRequest(Long receiverId, BigDecimal amount) {
        this.receiverId = receiverId;
        this.amount = amount;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
