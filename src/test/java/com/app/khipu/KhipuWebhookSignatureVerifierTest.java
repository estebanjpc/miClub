package com.app.khipu;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class KhipuWebhookSignatureVerifierTest {

	private final KhipuWebhookSignatureVerifier verifier = new KhipuWebhookSignatureVerifier();

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(verifier, "merchantSecret", "mi-secreto-test");
		ReflectionTestUtils.setField(verifier, "maxTimestampSkewMs", 300_000L);
	}

	@Test
	void aceptaFirmaCuandoCuerpoYCabeceraCoinciden() throws Exception {
		String body = "{\"payment_id\":\"abc123\",\"status\":\"done\"}";
		String t = String.valueOf(System.currentTimeMillis());
		String s = firmar("mi-secreto-test", t, body);
		String header = "t=" + t + ",s=" + s;

		assertThat(verifier.isValid(body, header)).isTrue();
	}

	@Test
	void rechazaSiElCuerpoFueAlterado() throws Exception {
		String bodyOriginal = "{\"payment_id\":\"abc123\",\"status\":\"done\"}";
		String t = String.valueOf(System.currentTimeMillis());
		String s = firmar("mi-secreto-test", t, bodyOriginal);
		String header = "t=" + t + ",s=" + s;

		String bodyManipulado = "{\"payment_id\":\"otro\",\"status\":\"done\"}";
		assertThat(verifier.isValid(bodyManipulado, header)).isFalse();
	}

	@Test
	void rechazaSecretoIncorrecto() throws Exception {
		String body = "{\"payment_id\":\"x\"}";
		String t = String.valueOf(System.currentTimeMillis());
		String s = firmar("otro-secreto", t, body);
		String header = "t=" + t + ",s=" + s;

		assertThat(verifier.isValid(body, header)).isFalse();
	}

	@Test
	void rechazaCabeceraVaciaOMalformada() {
		ReflectionTestUtils.setField(verifier, "merchantSecret", "s");
		assertThat(verifier.isValid("{}", "")).isFalse();
		assertThat(verifier.isValid("{}", "solo-texto")).isFalse();
		assertThat(verifier.isValid("{}", "t=123")).isFalse();
	}

	@Test
	void rechazaSecretoComercioVacio() {
		ReflectionTestUtils.setField(verifier, "merchantSecret", "");
		assertThat(verifier.isValid("{}", "t=1,s=abc")).isFalse();
	}

	@Test
	void rechazaTimestampFueraDeVentana() throws Exception {
		String body = "{}";
		long viejo = System.currentTimeMillis() - 400_000L;
		String s = firmar("mi-secreto-test", String.valueOf(viejo), body);
		String header = "t=" + viejo + ",s=" + s;

		assertThat(verifier.isValid(body, header)).isFalse();
	}

	@Test
	void sinComprobacionAntireplaySiVentanaEsCero() throws Exception {
		ReflectionTestUtils.setField(verifier, "maxTimestampSkewMs", 0L);
		String body = "{\"payment_id\":\"z\"}";
		long viejo = System.currentTimeMillis() - 10_000_000L;
		String s = firmar("mi-secreto-test", String.valueOf(viejo), body);
		String header = "t=" + viejo + ",s=" + s;

		assertThat(verifier.isValid(body, header)).isTrue();
	}

	private static String firmar(String secret, String t, String body) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		String toSign = t + "." + body;
		byte[] raw = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
		return Base64.getEncoder().encodeToString(raw);
	}

}
