package com.app.auth;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.app.entity.Usuario;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	@Autowired
	private IUsuarioService usuarioService;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {

		String email = authentication.getName();

		List<Usuario> usuarios = usuarioService.findAllByEmail(email);

		List<Usuario> activos = usuarios.stream().filter(u -> Boolean.TRUE.equals(u.getEnabled())).filter(u -> {
			boolean esAdmin = u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));

			if (esAdmin) {
				return true;
			}

			return u.getClub() != null && "1".equals(u.getClub().getEstado());
		}).toList();

		if (activos.isEmpty()) {
			response.sendRedirect("/sinClub");
			return;
		}

		if (activos.size() == 1) {
			setContexto(request, activos.get(0));
			response.sendRedirect(determinarRedirect(activos.get(0)));
			return;
		}

		// varios clubes → SOLO los suyos
		request.getSession().setAttribute("usuariosClub", activos);
		response.sendRedirect("/seleccionarClub");
	}

	private void setContexto(HttpServletRequest request, Usuario usuario) {
		request.getSession().setAttribute("usuarioLogin", usuario);
		request.getSession().setAttribute("rolesActivos", usuario.getRoles());
		if (usuario.getClub() != null) request.getSession().setAttribute("idClubSession", usuario.getClub().getId());
//		else request.getSession().removeAttribute("idClubSession");
		
	}

	private String determinarRedirect(Usuario usuario) {

		if (usuario.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority())))
			return "/listadoClub";

		// Debe ir antes que esPersonalClub: club/tesorero/entrenador con clave temporal iban siempre a listadoDeportistas
		if ("0".equals(usuario.getEstado())) {
			return "/actualizarPass";
		}

		if (esPersonalClub(usuario))
			return "/listadoDeportistas";

		return "/consulta";
	}

	/** Director técnico, tesorero o administración de club: mismo punto de entrada por ahora. */
	private boolean esPersonalClub(Usuario usuario) {
		return usuario.getRoles().stream().anyMatch(r -> {
			String a = r.getAuthority();
			return "ROLE_CLUB".equals(a) || "ROLE_TESORERO".equals(a) || "ROLE_ENTRENADOR".equals(a);
		});
	}
}
