package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.CobroAdicionalForm;
import com.app.service.ICategoriaService;
import com.app.service.IDeportistaService;
import com.app.service.IPagoService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/cobros-adicionales")
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class CobroAdicionalController {

	private final IUsuarioService usuarioService;
	private final IDeportistaService deportistaService;
	private final ICategoriaService categoriaService;
	private final IPagoService pagoService;

	public CobroAdicionalController(IUsuarioService usuarioService, IDeportistaService deportistaService,
			ICategoriaService categoriaService, IPagoService pagoService) {
		this.usuarioService = usuarioService;
		this.deportistaService = deportistaService;
		this.categoriaService = categoriaService;
		this.pagoService = pagoService;
	}

	@GetMapping
	public String vista(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		if (!model.containsAttribute("form")) {
			CobroAdicionalForm form = new CobroAdicionalForm();
			form.setTipoCobro(CobroAdicionalForm.TipoCobro.MATRICULA);
			form.setAlcance(CobroAdicionalForm.Alcance.TODOS);
			form.setMes(LocalDate.now().getMonthValue());
			form.setAnio(LocalDate.now().getYear());
			model.addAttribute("form", form);
		}
		model.addAttribute("titulo", "Cobros adicionales");
		model.addAttribute("deportistas", deportistaService.buscarPorClub(idClubSession));
		model.addAttribute("categorias",
				categoriaService.findByClub(usuarioService.resolveUsuarioActivo(principal.getName(), idClubSession).getClub()));
		model.addAttribute("cobros", pagoService.listarCobrosAdicionalesClub(idClubSession));
		return "cobrosAdicionales";
	}

	@PostMapping("/crear")
	public String crear(@Valid @ModelAttribute("form") CobroAdicionalForm form, BindingResult result,
			HttpServletRequest request, Principal principal, RedirectAttributes flash, Model model) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		if (form.getTipoCobro() == CobroAdicionalForm.TipoCobro.OTRO
				&& (form.getDescripcion() == null || form.getDescripcion().isBlank())) {
			result.rejectValue("descripcion", "NotBlank", "La descripción es obligatoria para tipo OTRO.");
		}
		if (form.getAlcance() == CobroAdicionalForm.Alcance.DEPORTISTA && form.getDeportistaId() == null) {
			result.rejectValue("deportistaId", "NotNull", "Debe seleccionar un deportista.");
		}
		if (form.getAlcance() == CobroAdicionalForm.Alcance.CATEGORIA && form.getCategoriaId() == null) {
			result.rejectValue("categoriaId", "NotNull", "Debe seleccionar una categoría.");
		}
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Cobros adicionales");
			model.addAttribute("deportistas", deportistaService.buscarPorClub(idClubSession));
			model.addAttribute("categorias",
					categoriaService.findByClub(usuarioService.resolveUsuarioActivo(principal.getName(), idClubSession).getClub()));
			model.addAttribute("cobros", pagoService.listarCobrosAdicionalesClub(idClubSession));
			return "cobrosAdicionales";
		}
		try {
			int total = pagoService.crearCobroAdicional(idClubSession, form);
			flash.addFlashAttribute("msjLayout", "success;Cobros creados;Se generaron " + total + " cobro(s) adicional(es).");
		} catch (IllegalArgumentException e) {
			flash.addFlashAttribute("msjLayout", "error;Error;" + e.getMessage());
		}
		return "redirect:/cobros-adicionales";
	}
}
