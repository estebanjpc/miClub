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

/**
 * Verificación HMAC-SHA256 del webhook según documentación Khipu (cabecera x-khipu-signature).
 * El cuerpo debe ser exactamente el JSON recibido, sin reordenar ni reformatear.
 */
@Component
public class KhipuWebhookSignatureVerifier {

	@Value("${khipu.merchant.secret:}")
	private String merchantSecret;

	@Value("${khipu.webhook.max-timestamp-skew-ms:300000}")
	private long maxTimestampSkewMs;

	public boolean isValid(String rawJsonBody, String xKhipuSignatureHeader) {
		if (!StringUtils.hasText(merchantSecret)) {
			return false;
		}
		if (!StringUtils.hasText(xKhipuSignatureHeader)) {
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
			return false;
		}

		if (maxTimestampSkewMs > 0) {
			try {
				long tMs = Long.parseLong(tValue);
				long now = System.currentTimeMillis();
				if (Math.abs(now - tMs) > maxTimestampSkewMs) {
					return false;
				}
			} catch (@SuppressWarnings("unused") NumberFormatException e) {
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
			return false;
		}

		byte[] receivedMac;
		try {
			receivedMac = Base64.getDecoder().decode(sValue);
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			return false;
		}

		return MessageDigest.isEqual(expectedMac, receivedMac);
	}

}
