package com.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.app.dto.KhipuResponse;
import com.app.entity.Pago;

@Service
public class KhipuServiceImpl implements IKhipuService {

    @Value("${khipu.api.url}")
    private String khipuApiUrl;

    @Value("${khipu.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public KhipuResponse crearPago(Integer montoEnPesos, List<Pago> pagos, Long ordenId) {

        Map<String, Object> body = new HashMap<>();
        body.put("subject", "Pago orden #" + ordenId);
        body.put("amount", montoEnPesos);
        body.put("currency", "CLP");
        body.put("transaction_id", "ORDEN-" + ordenId + "-" + UUID.randomUUID());
        body.put("return_url", "https://9bb4cd3f8f4c.ngrok-free.app/pago/ok?orden=" + ordenId);
        body.put("cancel_url", "https://9bb4cd3f8f4c.ngrok-free.app/pago/cancelado?orden=" + ordenId);
        body.put("notify_url", "https://9bb4cd3f8f4c.ngrok-free.app/api/khipu/notify");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
//                khipuApiUrl,
        		"https://payment-api.khipu.com/v3/payments",
                request,
                Map.class
        );

        Map<String, Object> resp = response.getBody();

        KhipuResponse khipuResponse = new KhipuResponse();
        khipuResponse.setPaymentId((String) resp.get("payment_id"));
        khipuResponse.setPaymentUrl((String) resp.get("payment_url"));

        return khipuResponse;
    }
}