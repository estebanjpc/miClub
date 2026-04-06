package com.app.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.app.khipu.KhipuWebhookSignatureVerifier;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;

@WebMvcTest(controllers = KhipuCallbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
		"khipu.webhook.verify-signature=true",
		"khipu.merchant.secret="
})
class KhipuCallbackControllerMerchantSecretIT {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IOrdenPagoService ordenPagoService;

	@MockBean
	private IPagoService pagoService;

	@MockBean
	private KhipuWebhookSignatureVerifier signatureVerifier;

	@Test
	void verificacionActivadaSinSecretoComercio_devuelve503() throws Exception {
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isServiceUnavailable())
				.andExpect(content().string("MERCHANT_SECRET_NOT_CONFIGURED"));
	}

}
