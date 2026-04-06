package com.app.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Club;
import com.app.entity.Usuario;
import com.app.service.IClubService;
import com.app.service.AsyncEmailService;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@Autowired
	private AsyncEmailService asyncEmailService;

	@Autowired
	private IClubService clubService;

	@GetMapping({ "/login", "/" })
	public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String logout,
			Model model, HttpServletRequest request) {

		model.addAttribute("titulo", "Login");

		String msj = (String) request.getSession().getAttribute("msjLogin");
		if (msj != null) {
			model.addAttribute("msjLogin", msj);
			request.getSession().removeAttribute("msjLogin");
		}

		if (logout != null) {
			model.addAttribute("success", "Ha cerrado sesión con éxito");
		}

		return "login";
	}

	@GetMapping({ "/recuperacion" })
	public String recuperacion(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		model.addAttribute("titulo", "Recuperar Clave");

		return "recuperarClave";
	}

	@PostMapping("/recuperarClave")
	public String recuperarClave(@RequestParam String email, Map<String, Object> model, RedirectAttributes flash,
			SessionStatus status) {

		model.put("titulo", "Recuperar Clave");
		if (!StringUtils.hasText(email) || email.length() > 254 || !email.contains("@")) {
			flash.addFlashAttribute("msjLogin", "error;Email;Ingrese un correo válido.");
			return "redirect:/login";
		}
		List<Usuario> usuarios = usuarioService.findAllByEmail(email.trim());

		if (usuarios == null || usuarios.isEmpty()) {
			flash.addFlashAttribute("msjLogin", "error;Usuario no existe;Usuario no existe con los datos ingresados");
		} else {
			String pass = Util.generatePassword(10);
			String encoded = passEncoder.encode(pass);
			for (Usuario u : usuarios) {
				u.setPassword(encoded);
				u.setEstado("0");
				usuarioService.save(u);
			}
			status.setComplete();
			Usuario paraCorreo = usuarios.get(0);
			paraCorreo.setPassAux(pass);
			asyncEmailService.recuperacionClave(paraCorreo);
			flash.addFlashAttribute("msjLogin",
					"success;Exito;Se envio un correo con una nueva contraseña para su ingreso");
		}

		return "redirect:/login";
	}

	@GetMapping({ "/actualizarPass" })
	public String actualizar(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		model.addAttribute("titulo", "Actualizar Clave");

		return "actualizar";
	}

	@PostMapping("/actualizarPass")
	public String actualizarPass(@RequestParam String password1, @RequestParam String password2, Principal principal,
			RedirectAttributes flash) {

		if (!password1.equals(password2)) {
			flash.addFlashAttribute("msjLayout", "error;Error;Las contraseñas no coinciden");
			return "redirect:/actualizarPass";
		}
		if (password1.length() < 8) {
			flash.addFlashAttribute("msjLayout", "error;Error;La contraseña debe tener al menos 8 caracteres.");
			return "redirect:/actualizarPass";
		}

		List<Usuario> usuarios = usuarioService.findAllByEmail(principal.getName());

		String encoded = passEncoder.encode(password1);

		for (Usuario u : usuarios) {
			u.setPassword(encoded);
			u.setEstado("1");
			usuarioService.save(u);
		}

		flash.addFlashAttribute("msjLayout", "success;Éxito;Contraseña actualizada correctamente");

		return "redirect:/login";
	}

	@GetMapping("/seleccionarClub")
	public String seleccionarClub(Model model, Principal principal) {
		List<Usuario> filas = usuarioService.findAllByEmail(principal.getName());
		Usuario usuario = filas.stream().filter(u -> Boolean.TRUE.equals(u.getEnabled())).findFirst()
				.orElse(filas.isEmpty() ? null : filas.get(0));
		List<Club> clubes = usuarioService.findClubesByUsuario(principal.getName());
		model.addAttribute("clubes", clubes);
		model.addAttribute("usuario", usuario);
		return "seleccionarClub";
	}

	@PostMapping("/setClubActivo")
	public String setClubActivo(@RequestParam Long clubId, RedirectAttributes flash, HttpServletRequest request,
			Principal principal) {

		boolean autorizado = usuarioService.findAllByEmail(principal.getName()).stream()
				.anyMatch(u -> Boolean.TRUE.equals(u.getEnabled()) && u.getClub() != null
						&& clubId.equals(u.getClub().getId()));
		if (!autorizado) {
			flash.addFlashAttribute("msjLogin", "error;Permisos;No pertenece a ese club.");
			return "redirect:/seleccionarClub";
		}

		Club club = clubService.findById(clubId);

		if (club == null || !"1".equals(club.getEstado())) {
			flash.addFlashAttribute("msjLogin",
					"error;Club deshabilitado;El club seleccionado se encuentra deshabilitado.");
			return "redirect:/seleccionarClub";
		}

		request.getSession().setAttribute("idClubSession", clubId);
		if (principal != null) {
			usuarioService.findAllByEmail(principal.getName()).stream()
					.filter(u -> u.getClub() != null && clubId.equals(u.getClub().getId()))
					.findFirst()
					.ifPresent(u -> request.getSession().setAttribute("usuarioLogin", u));
		}
		return "redirect:/consulta";
	}

}
