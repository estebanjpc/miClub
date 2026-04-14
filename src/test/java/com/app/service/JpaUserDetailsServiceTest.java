package com.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.repository.IUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

	@Mock
	private IUsuarioRepository usuarioRepository;

	@InjectMocks
	private JpaUserDetailsService jpaUserDetailsService;

	@Test
	void emailInexistente_lanzaUsernameNotFound() {
		when(usuarioRepository.findByEmail("nada@test.cl")).thenReturn(List.of());

		assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("nada@test.cl"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("EMAIL_NO_EXISTE");
	}

	@Test
	void ningunUsuarioHabilitado_devuelveUsuarioDeshabilitado() {
		Usuario u = usuario("a@test.cl", false, "ROLE_USER");
		when(usuarioRepository.findByEmail("a@test.cl")).thenReturn(List.of(u));

		UserDetails details = jpaUserDetailsService.loadUserByUsername("a@test.cl");

		assertThat(details.isEnabled()).isFalse();
		assertThat(details.getAuthorities()).isEmpty();
	}

	@Test
	void agregaTodasLasAutoridadesDeFilasHabilitadas() {
		Usuario u1 = usuario("x@test.cl", true, "ROLE_USER");
		Usuario u2 = usuario("x@test.cl", true, "ROLE_CLUB");
		when(usuarioRepository.findByEmail("x@test.cl")).thenReturn(List.of(u1, u2));

		UserDetails details = jpaUserDetailsService.loadUserByUsername("x@test.cl");

		assertThat(details.getUsername()).isEqualTo("x@test.cl");
		assertThat(details.getAuthorities()).extracting(a -> a.getAuthority())
				.containsExactlyInAnyOrder("ROLE_USER", "ROLE_CLUB");
	}

	private static Usuario usuario(String email, boolean enabled, String rol) {
		Usuario u = new Usuario();
		u.setEmail(email);
		u.setEnabled(enabled);
		u.setPassword("hash");
		u.setNombre("N");
		u.setEstado("1");
		u.getRoles().add(new Role("R", rol));
		return u;
	}

}
