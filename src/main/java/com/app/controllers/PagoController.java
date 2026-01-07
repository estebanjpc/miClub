package com.app.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.DashboardPagoDTO;
import com.app.dto.EstadoPagoDeportistaDTO;
import com.app.dto.MesPagoDTO;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.entity.Usuario;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;
import com.app.service.IClubService;
import com.app.service.IDashboardPagoService;
import com.app.service.IDeportistaService;
import com.app.service.IEstadoPagoClubService;
import com.app.service.IOrdenPagoService;
import com.app.service.IPagoService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class PagoController {

	@Autowired
	private IPagoService pagoService;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IDashboardPagoService dashboardService;

	@Autowired
	private IOrdenPagoService ordenPagoService;

	@Autowired
	private IDeportistaService deportistaService;

	@Autowired
	private IEstadoPagoClubService estadoPagoClubService;
	
	@Autowired
	private IClubService clubService;

	@GetMapping({ "/consulta" })
	public String consulta(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		model.addAttribute("titulo", "Consulta");
		
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		
		if (idClubSession == null) {
	        flash.addFlashAttribute("msjLayout", "error;Seleccione Club;No hay club seleccionado");
	        return "redirect:/login";
	    }

		String email = authentication.getName();
		Usuario usuario = usuarioService.findByEmail(email);

		List<MesPagoDTO> meses = pagoService.obtenerMesesParaPagar(usuario.getId(),idClubSession);
		List<Pago> pagosRealizados = pagoService.obtenerPagosRealizados(usuario.getId(),idClubSession);

		model.addAttribute("meses", meses);
		model.addAttribute("pagosRealizados", pagosRealizados);
		model.addAttribute("mediosPago", MedioPago.values());

		return "consulta";
	}

//	@GetMapping({ "/listadoPagos" })
//	public String listarPagosClub(@RequestParam(required = false) Integer mes, @RequestParam(required = false) EstadoPago estado,
//	        @RequestParam(required = false) Long idDeportista,Model model, Principal principal,HttpServletRequest request) {
//
//		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
//		List<Pago> listaDeportistas = pagoService.buscarPagosPorClub(idClubSession);
//		List<Pago> listaPagos = pagoService.buscarPagosFiltrados(idClubSession,mes,estado,idDeportista);
//		DashboardPagoDTO dashboard = dashboardService.obtenerResumen(idClubSession);
//		
//		Map<Integer, String> meses = new LinkedHashMap<>();
//	    for (Month m : Month.values()) {
//	        String nombre = m.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
//	        nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
//	        meses.put(m.getValue(), nombre);
//	    }
//
//		model.addAttribute("titulo", "Gestión de Pagos");
//		model.addAttribute("pagos", listaPagos);
//		model.addAttribute("deportistas",listaDeportistas);
//		model.addAttribute("dashboard",dashboard);
//		model.addAttribute("meses", meses);
//
//		return "listadoPagos";
//	}

	@GetMapping("/listadoPagos")
	public String listarPagosClub(@RequestParam(required = false) Integer mes,
			@RequestParam(required = false) EstadoPago estado, @RequestParam(required = false) Long idDeportista,
			Model model, HttpServletRequest request) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");

		Club club = clubService.findById(idClubSession);

		if (!"1".equals(club.getEstado())) {
			return "redirect:/login?clubDeshabilitado";
		}

		// Mes por defecto: mes actual
		if (mes == null) {
			mes = LocalDate.now().getMonthValue();
		}

		Integer anio = LocalDate.now().getYear();

		// NUEVO: listado principal (incluye morosos)
		List<EstadoPagoDeportistaDTO> estados = estadoPagoClubService.obtenerEstadoPorMes(idClubSession, mes, anio);

		// Filtros en memoria (simple y claro)
		if (estado != null) {
			estados = estados.stream().filter(e -> e.getEstado() == estado).toList();
		}

		if (idDeportista != null) {
			estados = estados.stream().filter(e -> e.getIdDeportista().equals(idDeportista)).toList();
		}

		// Dashboard
		DashboardPagoDTO dashboard = dashboardService.obtenerResumen(idClubSession);

		// Deportistas para filtro
		List<Deportista> deportistas = deportistaService.buscarPorClub(idClubSession);

		// Meses
		Map<Integer, String> meses = new LinkedHashMap<>();
		for (Month m : Month.values()) {
			String nombre = m.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
			nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
			meses.put(m.getValue(), nombre);
		}

		model.addAttribute("titulo", "Gestión de Pagos");
		model.addAttribute("estadoPagos", estados);
		model.addAttribute("dashboard", dashboard);
		model.addAttribute("deportistas", deportistas);
		model.addAttribute("meses", meses);

		model.addAttribute("mesSeleccionado", mes);
		model.addAttribute("estadoSeleccionado", estado != null ? estado.name() : null);
		model.addAttribute("deportistaSeleccionado", idDeportista);

		return "listadoPagos";
	}

	@PostMapping("/aprobar/{id}")
	public String aprobarPagoEfectivo(@PathVariable Long id, RedirectAttributes flash) {

		pagoService.aprobarPagoEfectivo(id);
		flash.addFlashAttribute("msjLayout", "success;Pago OK;Pago en efectivo aprobado correctamente");
		return "redirect:/listadoPagos";
	}

	@PostMapping("/pagar")
	public String pagar(@RequestParam(required = false) List<String> seleccionados,
			@RequestParam(name = "medioPago", required = false) String medioPagoStr, RedirectAttributes flash,
			Authentication authentication, HttpServletRequest request) {

		if (seleccionados == null || seleccionados.isEmpty()) {
			flash.addFlashAttribute("msjLayout", "error;Seleccione;Debe seleccionar al menos un mes para pagar");
			return "redirect:/consulta";
		}

		if (medioPagoStr == null) {
			flash.addFlashAttribute("msjLayout", "error;Seleccione;Debe seleccionar al menos un mes para pagar");
			return "redirect:/consulta";
		}

		String medioUpper = medioPagoStr.toUpperCase();

		if ("EFECTIVO".equals(medioUpper)) {
			pagoService.registrarPagoEfectivo(seleccionados);
			flash.addFlashAttribute("msjLayout", "success;Pago OK;Pago en efectivo registrado correctamente");
			return "redirect:/consulta";
		} else if ("KHIPU".equals(medioUpper)) {
			Usuario usuarioLogin = (Usuario) request.getSession().getAttribute("usuarioLogin");
			Long usuarioId = usuarioLogin.getId();
			OrdenPago orden = pagoService.generarOrdenPagoKhipu(seleccionados, usuarioId);
			return "redirect:" + orden.getKhipuUrl();
//            return "redirect:/consulta";
		} else {
			flash.addFlashAttribute("error", "Método de pago inválido");
			flash.addFlashAttribute("msjLayout", "error;Pago Invalido;Método de pago inválido");
			return "redirect:/consulta";
		}
	}

	@GetMapping("/orden/{id}")
	public String verOrdenPago(@PathVariable Long id, Model model) {

		OrdenPago orden = ordenPagoService.buscarPorId(id);

		model.addAttribute("orden", orden);
		return "/orden-detalle";
	}

	@GetMapping("/pago/khipu/retorno")
	public String khipuRetorno(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "PROCESANDO");
		return "khipuRetorno";
	}

	@GetMapping("/pago/ok")
	public String pagoOk(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "OK");
		return "khipuRetorno";
	}

	@GetMapping("/pago/cancelado")
	public String pagoCancelado(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "CANCELADO");
		return "khipuRetorno";
	}

}
