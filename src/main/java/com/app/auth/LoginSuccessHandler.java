package com.app.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.app.entity.Usuario;
import com.app.service.IUsuarioService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private IUsuarioService usuarioService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		String email = authentication.getName();
		Usuario usuario = usuarioService.findByEmail(email);

		// Guardar usuario completo en sesión
		request.getSession().setAttribute("usuarioLogin", usuario);

		// Si está bloqueado o debe cambiar pass
		if ("0".equalsIgnoreCase(usuario.getEstado())) {
			response.sendRedirect("/actualizarPass");
			return;
		}

		// Roles
		boolean esUser = usuario.getRoles().stream().anyMatch(r -> "ROLE_USER".equals(r.getAuthority()));
		boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
		boolean esClub = usuario.getRoles().stream().anyMatch(r -> "ROLE_CLUB".equals(r.getAuthority()));

		// Administrador
		if (esAdmin) {
			response.sendRedirect("/listadoClub");
			return;
		}

		if (esClub) {
			request.getSession().setAttribute("idClubSession", usuario.getClub().getId());
			response.sendRedirect("/listadoUsuarios");
			return;
		}

		if (esUser) {
			// Si es CLUB → validar multi-club
			List<Long> clubIds = usuarioService.findClubIdsByUsuario(email);

			if (clubIds.size() == 1) {
				request.getSession().setAttribute("idClubSession", clubIds.get(0));
				response.sendRedirect("/consulta");
				return;
			}

			if (clubIds.size() > 1) {
				// usuario debe elegir
				response.sendRedirect("/seleccionarClub");
				return;
			}

			if (clubIds.size() == 0) {
				// usuario debe elegir
				response.sendRedirect("/sinClub");
				return;
			}

		}

		super.onAuthenticationSuccess(request, response, authentication);
	}
}
