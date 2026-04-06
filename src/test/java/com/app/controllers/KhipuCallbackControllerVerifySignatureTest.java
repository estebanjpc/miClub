package com.app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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
		"khipu.merchant.secret=clave-configurada"
})
class KhipuCallbackControllerVerifySignatureTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IOrdenPagoService ordenPagoService;

	@MockBean
	private IPagoService pagoService;

	@MockBean
	private KhipuWebhookSignatureVerifier signatureVerifier;

	@Test
	void sinCabeceraFirma_devuelve401() throws Exception {
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().string("MISSING_SIGNATURE"));
	}

	@Test
	void firmaInvalida_devuelve401() throws Exception {
		when(signatureVerifier.isValid(any(), any())).thenReturn(false);

		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content("{}")
				.header("x-khipu-signature", "t=1,s=xxx")).andExpect(status().isUnauthorized())
				.andExpect(content().string("INVALID_SIGNATURE"));
	}

}
