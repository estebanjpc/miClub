package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.EmailCampanaForm;
import com.app.entity.Club;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.IEmailCampanaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("usuario")
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class CorreoClubController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private IEmailCampanaService emailCampanaService;

	@GetMapping("/correos")
	public String vistaCorreos(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		Club club = clubService.findById(idClubSession);
		if (club == null || !"1".equals(club.getEstado())) {
			return "redirect:/login?clubDeshabilitado";
		}

		if (!model.containsAttribute("campanaForm")) {
			EmailCampanaForm form = new EmailCampanaForm();
			form.setTipoEnvio(EmailCampanaForm.TipoEnvio.MASIVO);
			form.setMesPeriodo(LocalDate.now().getMonthValue());
			form.setAnioPeriodo(LocalDate.now().getYear());
			model.addAttribute("campanaForm", form);
		}

		model.addAttribute("titulo", "Comunicaciones por correo");
		model.addAttribute("club", club);
		model.addAttribute("categorias", categoriaService.findByClub(club));
		model.addAttribute("historialEnvios", emailCampanaService.historialPorClub(idClubSession));
		return "correosClub";
	}

	@PostMapping("/correos/enviar")
	public String enviarCorreo(@Valid @ModelAttribute("campanaForm") EmailCampanaForm form, BindingResult result,
			HttpServletRequest request, Principal principal, RedirectAttributes flash, Model model) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		if (form.getEdadMin() != null && form.getEdadMax() != null && form.getEdadMin() > form.getEdadMax()) {
			result.rejectValue("edadMin", "invalid", "Edad mínima no puede ser mayor que la edad máxima.");
		}

		boolean requierePeriodo = form.isSoloMorosos() || form.getTipoEnvio() == EmailCampanaForm.TipoEnvio.MOROSIDAD;
		if (requierePeriodo && (form.getMesPeriodo() == null || form.getAnioPeriodo() == null)) {
			result.reject("periodo", "Debe indicar mes y año para notificaciones de morosidad.");
		}

		if (result.hasErrors()) {
			model.addAttribute("org.springframework.validation.BindingResult.campanaForm", result);
			model.addAttribute("campanaForm", form);
			flash.addFlashAttribute("msjLayout", "error;Error de validación;Revisa los datos del envío.");
			return "redirect:/correos";
		}

		int total = emailCampanaService.enviarCampana(idClubSession, form);
		if (total == 0) {
			flash.addFlashAttribute("msjLayout", "warning;Sin destinatarios;No se encontraron socios que cumplan los filtros.");
		} else {
			flash.addFlashAttribute("msjLayout", "success;Envío realizado;Se enviaron correos a " + total + " destinatarios.");
		}
		return "redirect:/correos";
	}

	@ModelAttribute("estadosDeportista")
	public Map<String, String> estadosDeportista() {
		Map<String, String> estados = new HashMap<>();
		estados.put("", "Todos");
		estados.put("1", "Activo");
		estados.put("2", "Desactivo");
		return estados;
	}

	@ModelAttribute("tiposEnvio")
	public EmailCampanaForm.TipoEnvio[] tiposEnvio() {
		return EmailCampanaForm.TipoEnvio.values();
	}
}
