package com.app.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.app.config.TestMailConfig;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class PagoControllerSecurityIT {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@WithAnonymousUser
	void consulta_anonimo_redirigeALogin() throws Exception {
		mockMvc.perform(get("/consulta")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void consulta_apoderadoSinClubEnSesion_redirigeASeleccionarClub() throws Exception {
		mockMvc.perform(get("/consulta")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/seleccionarClub"));
	}

	@Test
	@WithMockUser(roles = "USER")
	void listadoPagos_apoderado_prohibido() throws Exception {
		mockMvc.perform(get("/listadoPagos")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLUB")
	void consulta_rolClub_prohibidoParaRutaApoderado() throws Exception {
		mockMvc.perform(get("/consulta")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "CLUB")
	void listadoPagos_rolClub_sinIdClubSesion_redirigeASeleccionar() throws Exception {
		mockMvc.perform(get("/listadoPagos")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/seleccionarClub"));
	}

	@Test
	@WithMockUser(roles = "ENTRENADOR")
	void listadoPagos_entrenador_prohibido() throws Exception {
		mockMvc.perform(get("/listadoPagos")).andExpect(status().isForbidden());
	}

}
