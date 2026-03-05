package com.fintech.Dto;

public class InvoiceEmailEvent {

    private Long invoiceId;
    private String recipientEmail;
    private String paymentLink;
    private String qrPath;

    public InvoiceEmailEvent(Long invoiceId, String recipientEmail, String paymentLink, String qrPath) {
        this.invoiceId = invoiceId;
        this.recipientEmail = recipientEmail;
        this.paymentLink = paymentLink;
        this.qrPath = qrPath;
    }

    public InvoiceEmailEvent() {
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public String getQrPath() {
        return qrPath;
    }

    public void setQrPath(String qrPath) {
        this.qrPath = qrPath;
    }
}