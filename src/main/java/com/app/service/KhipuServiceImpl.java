package com.app.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.app.dto.KhipuResponse;
import com.app.entity.CuentaBancaria;
import com.app.entity.Pago;

@Service
public class KhipuServiceImpl implements IKhipuService {

	@Value("${khipu.api.url:}")
	private String khipuApiUrlGlobal;

	@Value("${khipu.api.key:}")
	private String apiKeyGlobal;

	@Value("${app.public.url:http://localhost:8081}")
	private String appPublicUrl;

	private final RestTemplate restTemplate;

	private final ICuentaBancariaService cuentaBancariaService;

	public KhipuServiceImpl(@Qualifier("serviceRest") RestTemplate restTemplate,
			ICuentaBancariaService cuentaBancariaService) {
		this.restTemplate = restTemplate;
		this.cuentaBancariaService = cuentaBancariaService;
	}

	private String basePublicUrl() {
		String u = StringUtils.hasText(appPublicUrl) ? appPublicUrl.trim() : "http://localhost:8081";
		if (u.endsWith("/")) {
			return u.substring(0, u.length() - 1);
		}
		return u;
	}

	@Override
	public KhipuResponse crearPago(Integer montoEnPesos, List<Pago> pagos, Long ordenId, Long idClub) {

		CuentaBancaria cuenta = cuentaBancariaService.findByClubId(idClub);
		String apiKey = cuenta != null && StringUtils.hasText(cuenta.getKhipuApiKey()) ? cuenta.getKhipuApiKey().trim()
				: (apiKeyGlobal != null ? apiKeyGlobal.trim() : "");
		if (!StringUtils.hasText(apiKey)) {
			throw new IllegalStateException(
					"Configure la API key de Khipu en Menú → Datos de cuenta (Cuenta bancaria del club).");
		}

		String urlBase = cuenta != null && StringUtils.hasText(cuenta.getKhipuApiUrl()) ? cuenta.getKhipuApiUrl().trim()
				: (StringUtils.hasText(khipuApiUrlGlobal) ? khipuApiUrlGlobal.trim()
						: "https://payment-api.khipu.com/v3/payments/");

		String base = basePublicUrl();
		Map<String, Object> body = new HashMap<>();
		body.put("subject", "Pago orden #" + ordenId);
		body.put("amount", montoEnPesos);
		body.put("currency", "CLP");
		body.put("transaction_id", "ORDEN-" + ordenId + "-" + UUID.randomUUID());
		body.put("return_url", base + "/pago/ok?orden=" + ordenId);
		body.put("cancel_url", base + "/pago/cancelado?orden=" + ordenId);
		body.put("notify_url", base + "/api/khipu/notify");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("x-api-key", apiKey);

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

		String url = StringUtils.hasText(urlBase) ? urlBase : "https://payment-api.khipu.com/v3/payments/";
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, request,
				new ParameterizedTypeReference<Map<String, Object>>() {
				});

		if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
			throw new IllegalStateException("Respuesta inválida al crear cobro en Khipu");
		}

		Map<String, Object> resp = response.getBody();

		KhipuResponse khipuResponse = new KhipuResponse();
		khipuResponse.setPaymentId((String) resp.get("payment_id"));
		khipuResponse.setPaymentUrl((String) resp.get("payment_url"));
		if (!StringUtils.hasText(khipuResponse.getPaymentId()) || !StringUtils.hasText(khipuResponse.getPaymentUrl())) {
			throw new IllegalStateException("Khipu no devolvió payment_id o payment_url");
		}

		return khipuResponse;
	}
}
