package com.app.helper;

import com.app.dto.KhipuPaymentRequest;
import com.app.dto.KhipuPaymentResponse;

public class KhipuCreatePaymentHelper {

    public KhipuPaymentRequest buildPaymentRequest(int total, String transactionId, String returnUrl) {

        KhipuPaymentRequest req = new KhipuPaymentRequest();
        req.setAmount(total);
        req.setSubject("Pago mensualidad MiClub");
        req.setCurrency("CLP");
        req.setTransactionId(transactionId);
        req.setReturnUrl(returnUrl);
        req.setCancelUrl(returnUrl + "?cancelled=true");

        return req;
    }

    public boolean isPaymentSuccess(KhipuPaymentResponse resp) {
        return resp != null && resp.getPaymentUrl() != null;
    }

}
