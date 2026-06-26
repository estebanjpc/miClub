package com.app.controllers;

import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Club;
import com.app.notification.domain.NotificationType;
import com.app.notification.dto.MassEmailFilter;
import com.app.notification.dto.MassEmailRequest;
import com.app.notification.service.MassEmailService;
import com.app.notification.service.NotificationConfigService;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.IDeportistaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@SessionAttributes("usuario")
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class NotificationClubController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private IDeportistaService deportistaService;

	@Autowired
	private NotificationConfigService notificationConfigService;

	@Autowired
	private MassEmailService massEmailService;

	@GetMapping("/notificaciones/config")
	public String configuracion(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
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

		notificationConfigService.ensureDefaultsForClub(idClubSession);
		model.addAttribute("titulo", "Configuración de notificaciones");
		model.addAttribute("club", club);
		model.addAttribute("configs", notificationConfigService.listForClub(idClubSession));
		return "notificacionesConfig";
	}

	@PostMapping("/notificaciones/config")
	public String guardarConfiguracion(
			@RequestParam(required = false, defaultValue = "false") boolean beforeDueEnabled,
			@RequestParam int beforeDueDays,
			@RequestParam(required = false, defaultValue = "false") boolean afterDueEnabled,
			@RequestParam int afterDueDays,
			@RequestParam(required = false, defaultValue = "false") boolean paymentReceivedEnabled,
			HttpServletRequest request,
			Principal principal,
			RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		try {
			notificationConfigService.update(idClubSession, NotificationType.BEFORE_DUE, beforeDueEnabled, beforeDueDays);
			notificationConfigService.update(idClubSession, NotificationType.AFTER_DUE, afterDueEnabled, afterDueDays);
			notificationConfigService.update(idClubSession, NotificationType.PAYMENT_RECEIVED, paymentReceivedEnabled, 0);
			flash.addFlashAttribute("msjLayout", "success;Guardado;La configuración de notificaciones se actualizó correctamente.");
		} catch (Exception e) {
			flash.addFlashAttribute("msjLayout", "error;Error;No se pudo guardar la configuración.");
		}
		return "redirect:/notificaciones/config";
	}

	@GetMapping("/notificaciones/correo-masivo")
	public String correoMasivo(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
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

		YearMonth ym = YearMonth.now();
		int mes = ym.getMonthValue();
		int anio = ym.getYear();

		model.addAttribute("titulo", "Envío masivo de correos");
		model.addAttribute("club", club);
		model.addAttribute("categorias", categoriaService.findByClub(club));
		model.addAttribute("deportistas", deportistaService.listarTodosPorClub(idClubSession));
		model.addAttribute("mesDefault", mes);
		model.addAttribute("anioDefault", anio);
		model.addAttribute("morososCount", massEmailService.contarMorosos(idClubSession, mes, anio));
		model.addAttribute("morosos", massEmailService.listarMorosos(idClubSession, mes, anio));
		return "notificacionesCorreoMasivo";
	}

	@PostMapping("/notificaciones/correo-masivo/enviar")
	public String enviarCorreoMasivo(
			@RequestParam String subject,
			@RequestParam String message,
			@RequestParam MassEmailFilter filter,
			@RequestParam(required = false) List<Long> categoryIds,
			@RequestParam(required = false) List<Long> selectedDeportistaIds,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			HttpServletRequest request,
			Principal principal,
			RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		MassEmailRequest req = new MassEmailRequest();
		req.setSubject(subject);
		req.setMessage(message);
		req.setFilter(filter);
		req.setCategoryIds(categoryIds);
		req.setSelectedDeportistaIds(selectedDeportistaIds);
		req.setMonth(month);
		req.setYear(year);

		try {
			if (filter == MassEmailFilter.CATEGORY && (categoryIds == null || categoryIds.isEmpty())) {
				flash.addFlashAttribute("msjLayout", "error;Categorías;Selecciona al menos una categoría.");
				return "redirect:/notificaciones/correo-masivo";
			}
			if (filter == MassEmailFilter.CUSTOM && (selectedDeportistaIds == null || selectedDeportistaIds.isEmpty())) {
				flash.addFlashAttribute("msjLayout", "error;Destinatarios;Selecciona al menos un deportista.");
				return "redirect:/notificaciones/correo-masivo";
			}
			int total = massEmailService.enviar(idClubSession, req);
			if (total == 0) {
				flash.addFlashAttribute("msjLayout",
						"warning;Sin destinatarios;No hay correos válidos para los filtros elegidos.");
			} else {
				flash.addFlashAttribute("msjLayout",
						"success;Envío encolado;Se programaron correos para " + total + " destinatario(s) único(s).");
			}
		} catch (IllegalArgumentException ex) {
			flash.addFlashAttribute("msjLayout", "error;Datos inválidos;" + ex.getMessage());
		}
		return "redirect:/notificaciones/correo-masivo";
	}
}
