package com.fintech.Model.Entity;

import com.fintech.Model.Enum.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {

    private Long id;
    private Long userId;
    private BigDecimal amount;
    private String description;
    private String paymentToken;
    private String qrCodePath;
    private InvoiceStatus status;
    private LocalDateTime expiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Invoice() {
    }

    public Invoice(Long id,
                   Long userId,
                   BigDecimal amount,
                   String description,
                   String paymentToken,
                   String qrCodePath,
                   InvoiceStatus status,
                   LocalDateTime expiryTime,
                   LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.paymentToken = paymentToken;
        this.qrCodePath = qrCodePath;
        this.status = status;
        this.expiryTime = expiryTime;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(String qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(LocalDateTime expiryTime) {
        this.expiryTime = expiryTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}