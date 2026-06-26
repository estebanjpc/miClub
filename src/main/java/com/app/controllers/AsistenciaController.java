package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.AsistenciaEstadisticaDTO;
import com.app.entity.Deportista;
import com.app.entity.Usuario;
import com.app.service.IAsistenciaClaseService;
import com.app.service.IDeportistaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AsistenciaController {

	private final IAsistenciaClaseService asistenciaService;
	private final IDeportistaService deportistaService;
	private final IUsuarioService usuarioService;

	public AsistenciaController(IAsistenciaClaseService asistenciaService, IDeportistaService deportistaService,
			IUsuarioService usuarioService) {
		this.asistenciaService = asistenciaService;
		this.deportistaService = deportistaService;
		this.usuarioService = usuarioService;
	}

	@GetMapping("/asistencia/registro")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String registro(Model model, HttpServletRequest request, Principal principal,
			@RequestParam(required = false) String fecha, RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		LocalDate fechaClase = (fecha != null && !fecha.isBlank()) ? LocalDate.parse(fecha) : LocalDate.now();
		List<Deportista> deportistas = deportistaService.buscarPorClub(idClubSession);
		Set<Long> presentes = asistenciaService.obtenerPresentesEnFecha(idClubSession, fechaClase);
		model.addAttribute("titulo", "Registro de asistencia");
		model.addAttribute("fechaClase", fechaClase);
		model.addAttribute("deportistas", deportistas);
		model.addAttribute("presentes", presentes);
		return "asistenciaRegistro";
	}

	@PostMapping("/asistencia/registro")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String guardarRegistro(@RequestParam String fechaClase,
			@RequestParam(required = false) Set<Long> presentesIds,
			HttpServletRequest request,
			Authentication authentication,
			RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, authentication.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		asistenciaService.registrarAsistenciaDia(idClubSession, usuario.getId(), LocalDate.parse(fechaClase), presentesIds);
		flash.addFlashAttribute("msjLayout", "success;Asistencia guardada;Registro actualizado correctamente.");
		return "redirect:/asistencia/registro?fecha=" + fechaClase;
	}

	@GetMapping("/asistencia/estadisticas")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR", "ROLE_USER", "ROLE_SOCIO" })
	public String estadisticas(Model model, HttpServletRequest request, Principal principal,
			@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta,
			Authentication authentication,
			RedirectAttributes flash) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		LocalDate h = (hasta != null && !hasta.isBlank()) ? LocalDate.parse(hasta) : LocalDate.now();
		LocalDate d = (desde != null && !desde.isBlank()) ? LocalDate.parse(desde) : h.minusDays(30);
		boolean esApoderado = authentication.getAuthorities().stream()
				.anyMatch(a -> "ROLE_USER".equals(a.getAuthority()) || "ROLE_SOCIO".equals(a.getAuthority()));

		List<AsistenciaEstadisticaDTO> stats = esApoderado
				? asistenciaService.estadisticasUsuario(idClubSession, usuario.getId(), d, h)
				: asistenciaService.estadisticasClub(idClubSession, d, h);

		int totalClases = stats.stream().mapToInt(AsistenciaEstadisticaDTO::getClasesTotales).sum();
		int totalAsistencias = stats.stream().mapToInt(AsistenciaEstadisticaDTO::getAsistencias).sum();
		double pctGlobal = totalClases == 0 ? 0.0 : (totalAsistencias * 100.0 / totalClases);

		Map<String, Double> promedioPorCategoria = stats.stream()
				.collect(java.util.stream.Collectors.groupingBy(
						AsistenciaEstadisticaDTO::getCategoria,
						java.util.stream.Collectors.averagingDouble(AsistenciaEstadisticaDTO::getPorcentajeAsistencia)));

		model.addAttribute("titulo", "Estadísticas de asistencia");
		model.addAttribute("desde", d);
		model.addAttribute("hasta", h);
		model.addAttribute("stats", stats);
		model.addAttribute("totalClases", totalClases);
		model.addAttribute("totalAsistencias", totalAsistencias);
		model.addAttribute("pctGlobal", pctGlobal);
		model.addAttribute("promedioPorCategoria", promedioPorCategoria);
		model.addAttribute("esApoderado", esApoderado);
		return "asistenciaEstadisticas";
	}
}
