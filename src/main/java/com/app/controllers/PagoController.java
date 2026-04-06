package com.app.controllers;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
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
import com.app.dto.MorosidadClubDTO;
import com.app.dto.ResumenCategoriaDTO;
import com.app.entity.Categoria;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.entity.Usuario;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;
import com.app.service.ICategoriaService;
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
	private IDashboardPagoService dashboardService;

	@Autowired
	private IOrdenPagoService ordenPagoService;

	@Autowired
	private IDeportistaService deportistaService;

	@Autowired
	private IEstadoPagoClubService estadoPagoClubService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private IUsuarioService usuarioService;

	@GetMapping({ "/consulta" })
	@Secured("ROLE_USER")
	public String consulta(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		model.addAttribute("titulo", "Consulta");

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");

		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Seleccione Club;No hay club seleccionado");
			return "redirect:/seleccionarClub";
		}

		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, authentication.getName());
		if (usuario == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		List<MesPagoDTO> meses = pagoService.obtenerMesesParaPagar(usuario.getId(), idClubSession);
		List<Pago> pagosRealizados = pagoService.obtenerPagosRealizados(usuario.getId(), idClubSession);

		model.addAttribute("meses", meses);
		model.addAttribute("pagosRealizados", pagosRealizados);
		model.addAttribute("mediosPago", MedioPago.values());
		model.addAttribute("clubUsuario", usuario.getClub().getNombre());

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
	@Secured("ROLE_CLUB")
	public String listarPagosClub(@RequestParam(required = false) Integer mes,
			@RequestParam(required = false) EstadoPago estado, @RequestParam(required = false) Long idDeportista,
			@RequestParam(required = false) Long idCategoria,
			@RequestParam(required = false, defaultValue = "3") Integer minCuotasMorosos,
			@RequestParam(required = false, defaultValue = "pendientes") String tab,
			Model model, HttpServletRequest request, RedirectAttributes flash) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		Club club = clubService.findById(idClubSession);
		if (club == null) {
			flash.addFlashAttribute("msjLayout", "error;Club;Club no encontrado.");
			return "redirect:/login";
		}

		if (!"1".equals(club.getEstado())) {
			return "redirect:/login?clubDeshabilitado";
		}

		Integer anio = LocalDate.now().getYear();
		Integer mesActual = LocalDate.now().getMonthValue();

		if (mes == null) {
			mes = mesActual;
		}

		int minCuotas = minCuotasMorosos == null ? 3 : Math.min(24, Math.max(1, minCuotasMorosos));

		List<EstadoPagoDeportistaDTO> estadosCompletos = estadoPagoClubService.obtenerEstadoPorMes(idClubSession, mes, anio,
				"MES");
		List<ResumenCategoriaDTO> resumenPorCategoria = buildResumenPorCategoria(estadosCompletos);

		List<EstadoPagoDeportistaDTO> estados = new ArrayList<>(estadosCompletos);

		if (estado != null) {
			estados = estados.stream().filter(e -> e.getEstado() == estado).toList();
		}

		if (idDeportista != null) {
			estados = estados.stream().filter(e -> e.getIdDeportista().equals(idDeportista)).toList();
		}

		if (idCategoria != null) {
			estados = estados.stream().filter(e -> idCategoria.equals(e.getIdCategoria())).toList();
		}

		DashboardPagoDTO dashboard = dashboardService.obtenerResumen(idClubSession, mes, anio);
		List<Deportista> deportistas = deportistaService.buscarPorClub(idClubSession);
		List<Categoria> categorias = categoriaService.findByClub(club);

		List<Pago> pendientesAprobacion = pagoService.obtenerPendientesAprobacion(idClubSession);
		if (idCategoria != null) {
			pendientesAprobacion = pendientesAprobacion.stream()
					.filter(p -> p.getDeportista().getCategoria() != null
							&& idCategoria.equals(p.getDeportista().getCategoria().getId()))
					.toList();
		}

		List<MorosidadClubDTO> morososCriticos = pagoService.obtenerMorososCriticos(idClubSession, mes, anio, minCuotas);
		if (idCategoria != null) {
			morososCriticos = morososCriticos.stream()
					.filter(m -> idCategoria.equals(m.getIdCategoria()))
					.toList();
		}

		long kpiPagadosMes = pagoService.contarPagadosEnMes(idClubSession, mes, anio);

		Map<Long, List<Pago>> historialesMorosos = morososCriticos.stream()
				.collect(Collectors.toMap(
						MorosidadClubDTO::getIdDeportista,
						dto -> pagoService.obtenerHistorialDeportistaClub(idClubSession, dto.getIdDeportista()),
						(a, b) -> a,
						LinkedHashMap::new));

		Map<Integer, String> meses = new LinkedHashMap<>();
		for (Month m : Month.values()) {
			String nombre = m.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
			nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
			meses.put(m.getValue(), nombre);
		}

		model.addAttribute("nombreMesConsulta", meses.get(mes));
		model.addAttribute("anioConsulta", anio);

		model.addAttribute("titulo", "Gestión de Pagos");
		model.addAttribute("estadoPagos", estados);
		model.addAttribute("pendientesAprobacion", pendientesAprobacion);
		model.addAttribute("morososCriticos", morososCriticos);
		model.addAttribute("historialesMorosos", historialesMorosos);
		model.addAttribute("kpiPendientes", (long) pendientesAprobacion.size());
		model.addAttribute("kpiMorosos3", (long) morososCriticos.size());
		model.addAttribute("kpiPagadosMes", kpiPagadosMes);
		model.addAttribute("dashboard", dashboard);
		model.addAttribute("deportistas", deportistas);
		model.addAttribute("categorias", categorias);
		model.addAttribute("resumenPorCategoria", resumenPorCategoria);
		model.addAttribute("meses", meses);
		model.addAttribute("activeTab", tab);

		model.addAttribute("mesSeleccionado", mes);
		model.addAttribute("estadoSeleccionado", estado != null ? estado.name() : null);
		model.addAttribute("deportistaSeleccionado", idDeportista);
		model.addAttribute("categoriaSeleccionada", idCategoria);
		model.addAttribute("minCuotasMorosos", minCuotas);

		return "listadoPagos";
	}

	private List<ResumenCategoriaDTO> buildResumenPorCategoria(List<EstadoPagoDeportistaDTO> lista) {
		Map<Long, ResumenCategoriaDTO> map = new LinkedHashMap<>();
		for (EstadoPagoDeportistaDTO e : lista) {
			Long key = e.getIdCategoria() != null ? e.getIdCategoria() : 0L;
			String nombre = e.getNombreCategoria() != null ? e.getNombreCategoria() : "Sin categoría";
			ResumenCategoriaDTO r = map.computeIfAbsent(key, k -> {
				ResumenCategoriaDTO dto = new ResumenCategoriaDTO();
				dto.setIdCategoria(k == 0L ? null : k);
				dto.setNombreCategoria(nombre);
				return dto;
			});
			if (e.getEstado() == EstadoPago.MOROSO) {
				r.setMorosos(r.getMorosos() + 1);
			} else if (e.getEstado() == EstadoPago.PENDIENTE || e.getEstado() == EstadoPago.PENDIENTE_KHIPU) {
				r.setPendientesPago(r.getPendientesPago() + 1);
			}
		}
		return map.values().stream()
				.sorted(Comparator.comparing(ResumenCategoriaDTO::getNombreCategoria,
						Comparator.nullsLast(String::compareToIgnoreCase)))
				.toList();
	}

	@PostMapping("/aprobar/{id}")
	@Secured("ROLE_CLUB")
	public String aprobarPagoEfectivo(@PathVariable Long id, RedirectAttributes flash, HttpServletRequest request) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		try {
			pagoService.aprobarPagoEfectivo(id, idClubSession);
		} catch (AccessDeniedException e) {
			flash.addFlashAttribute("msjLayout", "error;Permisos;No puede modificar este pago.");
			return "redirect:/listadoPagos?tab=pendientes";
		}
		flash.addFlashAttribute("msjLayout", "success;Pago OK;Pago en efectivo aprobado correctamente");
		return "redirect:/listadoPagos?tab=pendientes";
	}
	
	@PostMapping("/rechazar/{id}")
	@Secured("ROLE_CLUB")
	public String rechazarPagoEfectivo(@PathVariable Long id, @RequestParam(required = false) String observacion,
			RedirectAttributes flash, HttpServletRequest request) {
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		try {
			pagoService.rechazarYReactivarPago(id, observacion != null ? observacion : "", idClubSession);
		} catch (AccessDeniedException e) {
			flash.addFlashAttribute("msjLayout", "error;Permisos;No puede modificar este pago.");
			return "redirect:/listadoPagos?tab=pendientes";
		}
	    flash.addFlashAttribute("msjLayout", "warning;Pago Rechazado;Se registró el rechazo en el historial. El mes vuelve a quedar disponible para pagar.");
	    return "redirect:/listadoPagos?tab=pendientes";
	}

	@PostMapping("/pagar")
	@Secured("ROLE_USER")
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

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Seleccione Club;No hay club seleccionado");
			return "redirect:/seleccionarClub";
		}

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, authentication.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		String medioUpper = medioPagoStr.toUpperCase();

		if ("EFECTIVO".equals(medioUpper)) {
			try {
				pagoService.registrarPagoEfectivo(seleccionados, usuarioLogin.getId(), idClubSession);
			} catch (AccessDeniedException e) {
				flash.addFlashAttribute("msjLayout", "error;Permisos;No puede registrar pagos para esos datos.");
				return "redirect:/consulta";
			} catch (IllegalArgumentException e) {
				flash.addFlashAttribute("msjLayout", "error;Datos;Solicitud inválida.");
				return "redirect:/consulta";
			}
			flash.addFlashAttribute("msjLayout", "success;Pago OK;Pago en efectivo registrado correctamente");
			return "redirect:/consulta";
		} else if ("KHIPU".equals(medioUpper)) {
			try {
				OrdenPago orden = pagoService.generarOrdenPagoKhipu(seleccionados, usuarioLogin.getId(), idClubSession);
				return "redirect:" + orden.getKhipuUrl();
			} catch (AccessDeniedException e) {
				flash.addFlashAttribute("msjLayout", "error;Permisos;No puede pagar esos ítems.");
				return "redirect:/consulta";
			} catch (IllegalArgumentException e) {
				flash.addFlashAttribute("msjLayout", "error;Datos;Solicitud inválida.");
				return "redirect:/consulta";
			} catch (IllegalStateException e) {
				flash.addFlashAttribute("msjLayout", "error;Khipu;No se pudo iniciar el pago. Intente más tarde.");
				return "redirect:/consulta";
			}
		} else {
			flash.addFlashAttribute("error", "Método de pago inválido");
			flash.addFlashAttribute("msjLayout", "error;Pago Invalido;Método de pago inválido");
			return "redirect:/consulta";
		}
	}

	@GetMapping("/orden/{id}")
	@Secured("ROLE_USER")
	public String verOrdenPago(@PathVariable Long id, Model model, HttpServletRequest request,
			Authentication authentication, RedirectAttributes flash) {

		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, authentication.getName());
		if (usuario == null) {
			return "redirect:/seleccionarClub";
		}
		return ordenPagoService.buscarPorIdOptional(id).filter(o -> usuario.getId().equals(o.getIdUsuario()))
				.map(orden -> {
					model.addAttribute("orden", orden);
					return "/orden-detalle";
				}).orElseGet(() -> {
					flash.addFlashAttribute("msjLayout", "error;Permisos;No puede ver esta orden.");
					return "redirect:/consulta";
				});
	}

	@GetMapping("/pago/khipu/retorno")
	@Secured("ROLE_USER")
	public String khipuRetorno(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "PROCESANDO");
		return "khipuRetorno";
	}

	@GetMapping("/pago/ok")
	@Secured("ROLE_USER")
	public String pagoOk(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "OK");
		return "khipuRetorno";
	}

	@GetMapping("/pago/cancelado")
	@Secured("ROLE_USER")
	public String pagoCancelado(@RequestParam(name = "orden", required = false) Long ordenId, Model model) {
		model.addAttribute("ordenId", ordenId);
		model.addAttribute("estado", "CANCELADO");
		return "khipuRetorno";
	}

}
