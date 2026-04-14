package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Club;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.entity.Usuario;
import com.app.enums.EstadoPago;
import com.app.repository.ICategoriaRepository;
import com.app.repository.IDeportistaRepository;
import com.app.repository.IOrdenPagoRepository;
import com.app.repository.IUsuarioRepository;
import com.app.service.IAdminPanelService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Panel de administración de plataforma: detalle de club, incidencias de pagos, modo soporte (opcional).
 */
@Controller
@RequestMapping("/admin")
@Secured("ROLE_ADMIN")
public class AdminPanelController {

	@Autowired
	private IAdminPanelService adminPanelService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IUsuarioRepository usuarioRepository;

	@Autowired
	private IDeportistaRepository deportistaRepository;

	@Autowired
	private ICategoriaRepository categoriaRepository;

	@Autowired
	private IOrdenPagoRepository ordenPagoRepository;

	@Value("${admin.soporte.enabled:false}")
	private boolean soporteHabilitado;

	@GetMapping("/club/{id}")
	public String detalleClub(@PathVariable("id") Long idClub, Model model, RedirectAttributes flash) {
		Club club = adminPanelService.findClubById(idClub);
		if (club == null) {
			flash.addFlashAttribute("msjLayout", "error;Club;No existe el club indicado.");
			return "redirect:/listadoClub";
		}
		long nu = usuarioRepository.countByClub_Id(idClub);
		long nd = deportistaRepository.countByUsuario_Club_Id(idClub);
		long nc = categoriaRepository.countByClub_Id(idClub);
		Usuario contacto = usuarioService.findUsuarioByAuthority("ROLE_CLUB").stream()
				.filter(u -> u.getClub() != null && idClub.equals(u.getClub().getId())).findFirst().orElse(null);

		model.addAttribute("titulo", "Detalle del club");
		model.addAttribute("club", club);
		model.addAttribute("totalUsuarios", nu);
		model.addAttribute("totalDeportistas", nd);
		model.addAttribute("totalCategorias", nc);
		model.addAttribute("contacto", contacto);
		model.addAttribute("soporteHabilitado", soporteHabilitado);
		return "admin/clubDetalle";
	}

	@GetMapping("/incidencias-pagos")
	public String incidenciasPagos(Model model) {
		List<Pago> pagos = adminPanelService.listarIncidenciasPagosRecientes(150);
		List<OrdenPago> ordenesKhipu = ordenPagoRepository.findByEstadoOrderByFechaCreacionDesc(
				EstadoPago.PENDIENTE_KHIPU, PageRequest.of(0, 50));
		model.addAttribute("titulo", "Incidencias de pagos");
		model.addAttribute("pagosIncidencia", pagos);
		model.addAttribute("ordenesKhipuPendientes", ordenesKhipu);
		return "admin/incidenciasPagos";
	}

	/**
	 * Coloca el contexto de club en sesión para navegar como soporte (solo lectura en acciones mutantes).
	 * Desactivado por defecto: {@code admin.soporte.enabled=true}.
	 */
	@GetMapping("/soporte/entrar/{clubId}")
	public String entrarSoporte(@PathVariable Long clubId, HttpServletRequest request, RedirectAttributes flash) {
		if (!soporteHabilitado) {
			flash.addFlashAttribute("msjLayout", "error;Soporte;El acceso de soporte está deshabilitado.");
			return "redirect:/listadoClub";
		}
		Club club = adminPanelService.findClubById(clubId);
		if (club == null) {
			flash.addFlashAttribute("msjLayout", "error;Club;Club no encontrado.");
			return "redirect:/listadoClub";
		}
		request.getSession().setAttribute("idClubSession", clubId);
		request.getSession().setAttribute("adminSoporte", Boolean.TRUE);
		usuarioService.findUsuarioByAuthority("ROLE_CLUB").stream()
				.filter(u -> u.getClub() != null && clubId.equals(u.getClub().getId()))
				.findFirst()
				.ifPresent(u -> request.getSession().setAttribute("usuarioLogin", u));
		flash.addFlashAttribute("msjLayout",
				"info;Modo soporte;Estás viendo el club como soporte. Las acciones de aprobación están deshabilitadas.");
		return "redirect:/listadoPagos?tab=pendientes";
	}

	@GetMapping("/soporte/salir")
	public String salirSoporte(HttpServletRequest request, RedirectAttributes flash) {
		request.getSession().removeAttribute("idClubSession");
		request.getSession().removeAttribute("adminSoporte");
		request.getSession().removeAttribute("usuarioLogin");
		flash.addFlashAttribute("msjLayout", "success;Soporte;Saliste del modo soporte.");
		return "redirect:/listadoClub";
	}
}
