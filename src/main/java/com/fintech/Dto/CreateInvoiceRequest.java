package com.fintech.Dto;

import java.math.BigDecimal;

public class CreateInvoiceRequest {

    private BigDecimal amount;
    private String description;

    public CreateInvoiceRequest() {
    }

    public CreateInvoiceRequest(BigDecimal amount, String description) {
        this.amount = amount;
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
