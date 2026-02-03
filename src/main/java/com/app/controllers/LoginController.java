package com.app.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import com.app.service.IEmailService;
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
	private IEmailService emailService;

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
		Usuario usuario = usuarioService.findByEmail(email);

		if (null == usuario) {
			flash.addFlashAttribute("msjLogin", "error;Usuario no existe;Usuario no existe con los datos ingresados");
		} else {
			String pass = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(pass));
			usuario.setEstado("0");
			usuarioService.save(usuario);
			status.setComplete();
			usuario.setPassAux(pass);
			Executors.newSingleThreadExecutor().execute(() -> emailService.recuperacionClave(usuario));
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
		Usuario usuario = usuarioService.findByEmail(principal.getName());
		List<Club> clubes = usuarioService.findClubesByUsuario(principal.getName());
		model.addAttribute("clubes", clubes);
		model.addAttribute("usuario", usuario);
		return "seleccionarClub";
	}

	@PostMapping("/setClubActivo")
	public String setClubActivo(@RequestParam Long clubId, RedirectAttributes flash, HttpServletRequest request) {

		Club club = clubService.findById(clubId);

		if (!"1".equals(club.getEstado())) {
			flash.addFlashAttribute("msjLogin",
					"error;Club deshabilitado;El club seleccionado se encuentra deshabilitado.");
			return "redirect:/seleccionarClub";
		}

		request.getSession().setAttribute("idClubSession", clubId);
		return "redirect:/consulta";
	}

}
