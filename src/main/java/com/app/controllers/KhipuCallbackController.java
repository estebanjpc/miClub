package com.app.controllers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.entity.CuentaBancaria;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.khipu.KhipuWebhookSignatureVerifier;
import com.app.service.ICuentaBancariaService;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/khipu")
public class KhipuCallbackController {

	private static final Logger log = LoggerFactory.getLogger(KhipuCallbackController.class);
	private static final long RATE_LIMIT_WINDOW_MS = 60_000L;

	private final Map<String, RateWindow> requestCounterByIp = new ConcurrentHashMap<>();

	@Autowired
	private KhipuWebhookSignatureVerifier signatureVerifier;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private IOrdenPagoService ordenPagoService;

	@Autowired
	private IPagoService pagoService;

	@Autowired
	private ICuentaBancariaService cuentaBancariaService;

	@Value("${khipu.webhook.verify-signature:true}")
	private boolean verifySignature;

	@Value("${khipu.merchant.secret:}")
	private String merchantSecretGlobal;

	@Value("${khipu.webhook.rate-limit.max-requests-per-minute:60}")
	private int rateLimitPerMinute;

	@PostMapping("/notify")
	@Transactional
	public ResponseEntity<String> recibirNotificacion(@RequestBody String rawBody,
			@RequestHeader(value = "x-khipu-signature", required = false) String signatureHeader,
			HttpServletRequest request) {

		String clientIp = resolveClientIp(request);
		if (!allowRequest(clientIp)) {
			log.warn("Webhook Khipu bloqueado por rate limit. ip={}", clientIp);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("RATE_LIMIT_EXCEEDED");
		}

		JsonNode root;
		try {
			root = objectMapper.readTree(rawBody);
		} catch (Exception e) {
			log.warn("Webhook Khipu inválido: JSON malformado. ip={}", clientIp);
			return ResponseEntity.badRequest().body("INVALID_JSON");
		}

		String paymentIdPreview = textOrNull(root, "payment_id");

		if (verifySignature) {
			String secret = resolveMerchantSecretForWebhook(paymentIdPreview);
			if (!StringUtils.hasText(secret)) {
				log.error("Webhook Khipu rechazado: merchant secret no configurado (ni en BD ni en configuración). ip={}",
						clientIp);
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("MERCHANT_SECRET_NOT_CONFIGURED");
			}
			if (!StringUtils.hasText(signatureHeader)) {
				log.warn("Webhook Khipu rechazado: firma ausente. ip={}", clientIp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("MISSING_SIGNATURE");
			}
			if (!signatureVerifier.isValid(rawBody, signatureHeader, secret)) {
				log.warn("Webhook Khipu rechazado: firma inválida. ip={}", clientIp);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("INVALID_SIGNATURE");
			}
		}

		String paymentId = paymentIdPreview;
		String status = textOrNull(root, "status");
		String transactionId = textOrNull(root, "transaction_id");

		if (paymentId == null) {
			log.warn("Webhook Khipu inválido: payment_id ausente. ip={}", clientIp);
			return ResponseEntity.badRequest().body("MISSING_PAYMENT_ID");
		}

		if (!"done".equalsIgnoreCase(status)) {
			log.info("Webhook Khipu ignorado por estado no final. paymentId={} status={} ip={}", paymentId, status, clientIp);
			return ResponseEntity.ok("IGNORED");
		}

		OrdenPago orden = ordenPagoService.findByKhipuPaymentId(paymentId);

		if (orden == null) {
			log.warn("Webhook Khipu sin orden asociada. paymentId={} ip={}", paymentId, clientIp);
			return ResponseEntity.ok("ORDER_NOT_FOUND");
		}

		if (orden.getEstado() == EstadoPago.PAGADO) {
			log.info("Webhook Khipu duplicado ignorado. paymentId={} ordenId={}", paymentId, orden.getId());
			return ResponseEntity.ok("ALREADY_PAID");
		}

		orden.setEstado(EstadoPago.PAGADO);
		orden.setFechaPago(LocalDateTime.now());
		orden.setKhipuTransactionId(transactionId);

		for (Pago p : orden.getPagos()) {
			p.setEstado(EstadoPago.PAGADO);
			if (p.getMonto() == null && p.getDeportista() != null && p.getDeportista().getCategoria() != null) {
				p.setMonto(p.getDeportista().getCategoria().getValorCuota());
			}
			p.setObservacion("Pago confirmado por Khipu");
			pagoService.save(p);
		}

		ordenPagoService.save(orden);
		log.info("Webhook Khipu procesado OK. paymentId={} ordenId={} transactionId={}", paymentId, orden.getId(), transactionId);

		return ResponseEntity.ok("OK");
	}

	/**
	 * Secreto HMAC: primero el configurado en la cuenta bancaria del club (por payment_id → orden), si no el de
	 * application.properties.
	 */
	private String resolveMerchantSecretForWebhook(String paymentId) {
		if (StringUtils.hasText(paymentId)) {
			OrdenPago orden = ordenPagoService.findByKhipuPaymentIdWithDetalle(paymentId);
			if (orden != null && orden.getPagos() != null && !orden.getPagos().isEmpty()) {
				Pago primero = orden.getPagos().get(0);
				if (primero.getClub() != null) {
					CuentaBancaria cb = cuentaBancariaService.findByClubId(primero.getClub().getId());
					if (cb != null && StringUtils.hasText(cb.getKhipuMerchantSecret())) {
						return cb.getKhipuMerchantSecret().trim();
					}
				}
			}
		}
		return StringUtils.hasText(merchantSecretGlobal) ? merchantSecretGlobal.trim() : null;
	}

	private boolean allowRequest(String ip) {
		if (rateLimitPerMinute <= 0) {
			return true;
		}
		long now = System.currentTimeMillis();
		RateWindow current = requestCounterByIp.compute(ip, (key, previous) -> {
			if (previous == null || now - previous.windowStartMs > RATE_LIMIT_WINDOW_MS) {
				return new RateWindow(now, new AtomicInteger(1));
			}
			previous.counter.incrementAndGet();
			return previous;
		});
		requestCounterByIp.entrySet().removeIf(entry -> now - entry.getValue().windowStartMs > RATE_LIMIT_WINDOW_MS * 2);
		return current.counter.get() <= rateLimitPerMinute;
	}

	private String resolveClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (StringUtils.hasText(forwardedFor)) {
			return forwardedFor.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (StringUtils.hasText(realIp)) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}

	private static String textOrNull(JsonNode root, String field) {
		JsonNode n = root.get(field);
		if (n == null || n.isNull()) {
			return null;
		}
		return n.asText();
	}

	private static final class RateWindow {
		private final long windowStartMs;
		private final AtomicInteger counter;

		private RateWindow(long windowStartMs, AtomicInteger counter) {
			this.windowStartMs = windowStartMs;
			this.counter = counter;
		}
	}

}
