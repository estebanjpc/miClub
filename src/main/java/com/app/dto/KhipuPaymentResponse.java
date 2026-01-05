package com.app.dto;

public class KhipuPaymentResponse {

    private String paymentId;       // ID interno de Khipu (muy importante guardar)
    private String paymentUrl;      // URL donde el usuario paga
    private String simplifiedUrl;   // Variante simple de pago
    private String status;          // status inicial (usually "pending")

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getSimplifiedUrl() {
        return simplifiedUrl;
    }

    public void setSimplifiedUrl(String simplifiedUrl) {
        this.simplifiedUrl = simplifiedUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
