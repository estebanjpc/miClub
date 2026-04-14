package com.app.khipu;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verificación HMAC-SHA256 del webhook según documentación Khipu (cabecera x-khipu-signature).
 * El cuerpo debe ser exactamente el JSON recibido, sin reordenar ni reformatear.
 * El secreto del comercio se pasa por llamada (por club en BD o fallback global).
 */
@Component
public class KhipuWebhookSignatureVerifier {

	private static final Logger log = LoggerFactory.getLogger(KhipuWebhookSignatureVerifier.class);

	@Value("${khipu.webhook.max-timestamp-skew-ms:300000}")
	private long maxTimestampSkewMs;

	public boolean isValid(String rawJsonBody, String xKhipuSignatureHeader, String merchantSecret) {
		if (!StringUtils.hasText(merchantSecret)) {
			log.error("Webhook Khipu rechazado: merchant secret no configurado");
			return false;
		}
		if (!StringUtils.hasText(xKhipuSignatureHeader)) {
			log.warn("Webhook Khipu rechazado: header x-khipu-signature ausente");
			return false;
		}

		String tValue = null;
		String sValue = null;
		for (String part : xKhipuSignatureHeader.split(",")) {
			int eq = part.indexOf('=');
			if (eq <= 0) {
				continue;
			}
			String key = part.substring(0, eq).trim();
			String val = part.substring(eq + 1).trim();
			if ("t".equals(key)) {
				tValue = val;
			} else if ("s".equals(key)) {
				sValue = val;
			}
		}
		if (tValue == null || sValue == null) {
			log.warn("Webhook Khipu rechazado: header de firma incompleto");
			return false;
		}

		if (maxTimestampSkewMs > 0) {
			try {
				long tMs = Long.parseLong(tValue);
				long now = System.currentTimeMillis();
				if (Math.abs(now - tMs) > maxTimestampSkewMs) {
					log.warn("Webhook Khipu rechazado: timestamp fuera de ventana permitida");
					return false;
				}
			} catch (@SuppressWarnings("unused") NumberFormatException e) {
				log.warn("Webhook Khipu rechazado: timestamp inválido en firma");
				return false;
			}
		}

		String toSign = tValue + "." + rawJsonBody;
		byte[] expectedMac;
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(merchantSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			expectedMac = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			log.error("Error calculando HMAC de Khipu", e);
			return false;
		}

		byte[] receivedMac;
		try {
			receivedMac = Base64.getDecoder().decode(sValue);
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			log.warn("Webhook Khipu rechazado: firma no es Base64 válida");
			return false;
		}
		if (receivedMac.length != expectedMac.length) {
			log.warn("Webhook Khipu rechazado: largo de firma inválido");
			return false;
		}

		return MessageDigest.isEqual(expectedMac, receivedMac);
	}

}
