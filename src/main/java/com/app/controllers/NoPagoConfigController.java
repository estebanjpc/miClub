package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.NoPagoConfigForm;
import com.app.service.ICategoriaService;
import com.app.service.IDeportistaService;
import com.app.service.INoPagoConfigService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/no-pago")
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class NoPagoConfigController {

	private final INoPagoConfigService noPagoService;
	private final IUsuarioService usuarioService;
	private final ICategoriaService categoriaService;
	private final IDeportistaService deportistaService;

	public NoPagoConfigController(INoPagoConfigService noPagoService, IUsuarioService usuarioService,
			ICategoriaService categoriaService, IDeportistaService deportistaService) {
		this.noPagoService = noPagoService;
		this.usuarioService = usuarioService;
		this.categoriaService = categoriaService;
		this.deportistaService = deportistaService;
	}

	@GetMapping
	public String vista(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		var usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		if (!model.containsAttribute("form")) {
			NoPagoConfigForm form = new NoPagoConfigForm();
			form.setMes(LocalDate.now().getMonthValue());
			form.setAnio(LocalDate.now().getYear());
			form.setScope(NoPagoConfigForm.Scope.CLUB);
			model.addAttribute("form", form);
		}
		model.addAttribute("titulo", "Configuración de meses sin cobro");
		model.addAttribute("configs", noPagoService.listarPorClub(idClubSession));
		model.addAttribute("categorias", categoriaService.findByClub(usuario.getClub()));
		model.addAttribute("deportistas", deportistaService.buscarPorClub(idClubSession));
		return "noPagoConfig";
	}

	@PostMapping
	public String crear(@Valid @ModelAttribute("form") NoPagoConfigForm form, BindingResult result,
			HttpServletRequest request, Principal principal, RedirectAttributes flash, Model model) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		var usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Configuración de meses sin cobro");
			model.addAttribute("configs", noPagoService.listarPorClub(idClubSession));
			model.addAttribute("categorias", categoriaService.findByClub(usuario.getClub()));
			model.addAttribute("deportistas", deportistaService.buscarPorClub(idClubSession));
			return "noPagoConfig";
		}
		try {
			noPagoService.crear(idClubSession, form);
			flash.addFlashAttribute("msjLayout", "success;No pago creado;Configuración guardada.");
		} catch (IllegalArgumentException e) {
			flash.addFlashAttribute("msjLayout", "error;Error;" + e.getMessage());
		}
		return "redirect:/no-pago";
	}

	@PostMapping("/eliminar/{id}")
	public String eliminar(@PathVariable Long id, HttpServletRequest request, RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		try {
			noPagoService.eliminar(id, idClubSession);
			flash.addFlashAttribute("msjLayout", "success;Eliminado;Configuración eliminada.");
		} catch (IllegalArgumentException e) {
			flash.addFlashAttribute("msjLayout", "error;Error;" + e.getMessage());
		}
		return "redirect:/no-pago";
	}
}
