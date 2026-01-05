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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Club;
import com.app.entity.Usuario;
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

//	@GetMapping({ "/login", "/" })
//	public String login(@RequestParam(value = "error", required = false) String error,
//			@RequestParam(value = "logout", required = false) String logout, Model model, Principal principal,
//			RedirectAttributes flash, HttpServletRequest request) {
//
//		model.addAttribute("titulo", "Login");
//
//		if (principal != null) {
//			Usuario usuario = usuarioService.findByEmail(principal.getName());
//			request.getSession().setAttribute("usuarioLogin", usuario);
//
//			if (usuario.getEstado().equalsIgnoreCase("0"))
//				return "redirect:/actualizarPass";
//
//			boolean esUser = usuario.getRoles().stream().anyMatch(r -> "ROLE_USER".equals(r.getAuthority()));
//			boolean esAdmin = usuario.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
//			boolean esClub = usuario.getRoles().stream().anyMatch(r -> "ROLE_CLUB".equals(r.getAuthority()));
//
//			if (esUser) {
//				return "redirect:/consulta";
//			} else if (esAdmin) {
//				return "redirect:/listadoClub";
//			} else if (esClub) {
//				request.getSession().setAttribute("idClubSession", usuario.getClub().getId());
//				return "redirect:/listadoUsuarios";
//			}
//		}
//
//		String loginError = (String) request.getSession().getAttribute("loginError");
//		if (loginError != null) {
//			model.addAttribute("msjLogin",
//					"error;Error en el login; nombre de usuario o contraseña incorrecta, por favor vuelva a intentarlo!");
//			request.getSession().removeAttribute("loginError"); // limpiar
//		}
//
//		if (error != null) {
//			model.addAttribute("msjLogin",
//					"error;Error en el login; nombre de usuario o contraseña incorrecta, por favor vuelva a intentarlo!");
//		}
//
//		if (logout != null) {
//			model.addAttribute("success", "Ha cerrado session con exito");
//		}
//
//		return "login";
//	}

	@GetMapping({ "/login", "/" })
	public String login(
	        @RequestParam(value = "error", required = false) String error,
	        @RequestParam(value = "logout", required = false) String logout,
	        Model model,
	        HttpServletRequest request) {

	    model.addAttribute("titulo", "Login");

	    if (error != null) {
	        model.addAttribute("msjLogin",
	                "error;Error en el login; nombre de usuario o contraseña incorrecta, por favor vuelva a intentarlo!");
	    }

	    if (logout != null) {
	        model.addAttribute("success", "Ha cerrado sesión con éxito");
	    }

	    return "login";
	}
	
	@RequestMapping(value = { "/recuperacion" })
	public String recuperacion(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		model.addAttribute("titulo", "Recuperar Clave");

		return "recuperarClave";
	}

	@PostMapping("/recuperarClave")
	public String recuperarClave(@RequestParam("email") String email, Map<String, Object> model,
			RedirectAttributes flash, SessionStatus status) {

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
	public String actualizarPass(@RequestParam("password1") String password1,
			@RequestParam("password2") String password2, Map<String, Object> model, RedirectAttributes flash,
			SessionStatus status, Principal principal) {

		model.put("titulo", "Actualizar Clave");

		if (!password1.equalsIgnoreCase(password2)) {
			flash.addFlashAttribute("msjLayout", "error;Error Password;Password deben ser iguales");
			return "redirect:/actualizarPass";
		}

		Usuario usuario = usuarioService.findByEmail(principal.getName());

		usuario.setPassword(passEncoder.encode(password1));
		usuario.setEstado("1");
		usuarioService.save(usuario);
		status.setComplete();
		flash.addFlashAttribute("msjLayout", "success;Exito;Se realizo corrextamente el cambio de password");

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
	public String setClubActivo(@RequestParam("clubId") Long clubId, HttpServletRequest request) {
	    request.getSession().setAttribute("idClubSession", clubId);
	    return "redirect:/consulta";
	}

}
