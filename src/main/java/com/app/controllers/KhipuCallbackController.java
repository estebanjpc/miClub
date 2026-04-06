package com.app.controllers;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.khipu.KhipuWebhookSignatureVerifier;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/khipu")
public class KhipuCallbackController {

	@Autowired
	private KhipuWebhookSignatureVerifier signatureVerifier;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private IOrdenPagoService ordenPagoService;

	@Autowired
	private IPagoService pagoService;

	@Value("${khipu.webhook.verify-signature:true}")
	private boolean verifySignature;

	@Value("${khipu.merchant.secret:}")
	private String merchantSecret;

	@PostMapping("/notify")
	@Transactional
	public ResponseEntity<String> recibirNotificacion(@RequestBody String rawBody,
			@RequestHeader(value = "x-khipu-signature", required = false) String signatureHeader) {

		if (verifySignature) {
			if (!StringUtils.hasText(merchantSecret)) {
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("MERCHANT_SECRET_NOT_CONFIGURED");
			}
			if (!StringUtils.hasText(signatureHeader)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("MISSING_SIGNATURE");
			}
			if (!signatureVerifier.isValid(rawBody, signatureHeader)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_SIGNATURE");
			}
		}

		JsonNode root;
		try {
			root = objectMapper.readTree(rawBody);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("INVALID_JSON");
		}

		String paymentId = textOrNull(root, "payment_id");
		String status = textOrNull(root, "status");
		String transactionId = textOrNull(root, "transaction_id");

		if (paymentId == null) {
			return ResponseEntity.badRequest().body("MISSING_PAYMENT_ID");
		}

		if (!"done".equalsIgnoreCase(status)) {
			return ResponseEntity.ok("IGNORED");
		}

		OrdenPago orden = ordenPagoService.findByKhipuPaymentId(paymentId);

		if (orden == null) {
			return ResponseEntity.ok("ORDER_NOT_FOUND");
		}

		if (orden.getEstado() == EstadoPago.PAGADO) {
			return ResponseEntity.ok("ALREADY_PAID");
		}

		orden.setEstado(EstadoPago.PAGADO);
		orden.setFechaPago(LocalDateTime.now());
		orden.setKhipuTransactionId(transactionId);

		for (Pago p : orden.getPagos()) {
			p.setEstado(EstadoPago.PAGADO);
			p.setObservacion("Pago confirmado por Khipu");
			pagoService.save(p);
		}

		ordenPagoService.save(orden);

		return ResponseEntity.ok("OK");
	}

	private static String textOrNull(JsonNode root, String field) {
		JsonNode n = root.get(field);
		if (n == null || n.isNull()) {
			return null;
		}
		return n.asText();
	}

}
