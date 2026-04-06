package com.app.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.khipu.KhipuWebhookSignatureVerifier;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;

@WebMvcTest(controllers = KhipuCallbackController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
		"khipu.webhook.verify-signature=false",
		"khipu.merchant.secret="
})
class KhipuCallbackControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IOrdenPagoService ordenPagoService;

	@MockBean
	private IPagoService pagoService;

	@MockBean
	private KhipuWebhookSignatureVerifier signatureVerifier;

	@Test
	void estadoDistintoDeDone_devuelveIgnored() throws Exception {
		String json = "{\"payment_id\":\"pid1\",\"status\":\"pending\"}";
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(content().string("IGNORED"));

		verify(ordenPagoService, never()).findByKhipuPaymentId(any());
	}

	@Test
	void jsonInvalido_devuelveBadRequest() throws Exception {
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content("{no-json"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("INVALID_JSON"));
	}

	@Test
	void faltaPaymentId_devuelveBadRequest() throws Exception {
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"done\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("MISSING_PAYMENT_ID"));
	}

	@Test
	void ordenNoExiste_devuelveOrderNotFound() throws Exception {
		when(ordenPagoService.findByKhipuPaymentId("pidX")).thenReturn(null);

		String json = "{\"payment_id\":\"pidX\",\"status\":\"done\"}";
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(content().string("ORDER_NOT_FOUND"));
	}

	@Test
	void ordenYaPagada_devuelveAlreadyPaid() throws Exception {
		OrdenPago orden = new OrdenPago();
		orden.setEstado(EstadoPago.PAGADO);
		orden.setPagos(new ArrayList<>());
		when(ordenPagoService.findByKhipuPaymentId("pidY")).thenReturn(orden);

		String json = "{\"payment_id\":\"pidY\",\"status\":\"done\"}";
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(content().string("ALREADY_PAID"));

		verify(pagoService, never()).save(any());
	}

	@Test
	void pagoExitoso_persisteYdevuelveOk() throws Exception {
		Pago p = new Pago();
		p.setEstado(EstadoPago.PENDIENTE_KHIPU);
		List<Pago> pagos = new ArrayList<>();
		pagos.add(p);

		OrdenPago orden = new OrdenPago();
		orden.setEstado(EstadoPago.PENDIENTE_KHIPU);
		orden.setPagos(pagos);
		when(ordenPagoService.findByKhipuPaymentId("pidZ")).thenReturn(orden);

		String json = "{\"payment_id\":\"pidZ\",\"status\":\"done\",\"transaction_id\":\"tx-1\"}";
		mockMvc.perform(post("/api/khipu/notify").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk())
				.andExpect(content().string("OK"));

		verify(pagoService).save(p);
		verify(ordenPagoService).save(orden);
	}

}
