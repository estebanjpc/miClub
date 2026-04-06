package com.app.controllers;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.ConciliacionKhipuFilaDTO;
import com.app.dto.FinancieroDashboardDTO;
import com.app.dto.ReportePagoFilaDTO;
import com.app.entity.Categoria;
import com.app.entity.Club;
import com.app.entity.Usuario;
import com.app.enums.MedioPago;
import com.app.service.FinancieroExportService;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.IFinancieroClubService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/financiero")
@Secured("ROLE_CLUB")
public class FinancieroClubController {

	@Autowired
	private IFinancieroClubService financieroClubService;

	@Autowired
	private FinancieroExportService financieroExportService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private IUsuarioService usuarioService;

	@GetMapping
	public String dashboard(
			@RequestParam(required = false) Integer mesDesde,
			@RequestParam(required = false) Integer anioDesde,
			@RequestParam(required = false) Integer mesHasta,
			@RequestParam(required = false) Integer anioHasta,
			@RequestParam(required = false) Long idCategoria,
			@RequestParam(required = false) MedioPago medio,
			Model model,
			HttpServletRequest request,
			RedirectAttributes flash,
			Principal principal) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			flash.addFlashAttribute("msjLayout", "error;Sesión;Seleccione un club");
			return "redirect:/seleccionarClub";
		}

		Club club = clubService.findById(idClubSession);
		if (club == null || !"1".equals(club.getEstado())) {
			return "redirect:/login?clubDeshabilitado";
		}

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		LocalDate hoy = LocalDate.now();
		int md = mesDesde != null ? mesDesde : 1;
		int ad = anioDesde != null ? anioDesde : hoy.getYear();
		int mh = mesHasta != null ? mesHasta : 12;
		int ah = anioHasta != null ? anioHasta : hoy.getYear();

		FinancieroDashboardDTO dash = financieroClubService.obtenerDashboard(idClubSession, md, ad, mh, ah);
		List<ReportePagoFilaDTO> preview = financieroClubService.obtenerFilasReporte(idClubSession,
				dash.getMesDesde(), dash.getAnioDesde(), dash.getMesHasta(), dash.getAnioHasta(),
				idCategoria, medio);
		List<ConciliacionKhipuFilaDTO> khipu = financieroClubService.obtenerConciliacionKhipu(idClubSession);
		List<Categoria> categorias = categoriaService.findByClub(club);

		Map<Integer, String> meses = mesesDelAnio();
		List<Integer> anios = new ArrayList<>();
		for (int y = hoy.getYear() - 6; y <= hoy.getYear() + 1; y++) {
			anios.add(y);
		}

		model.addAttribute("titulo", "Dashboard financiero");
		model.addAttribute("anios", anios);
		model.addAttribute("clubNombre", club.getNombre());
		model.addAttribute("dashboard", dash);
		model.addAttribute("filasPreview", preview);
		model.addAttribute("conciliacionKhipu", khipu);
		model.addAttribute("categorias", categorias);
		model.addAttribute("mediosPago", MedioPago.values());
		model.addAttribute("meses", meses);
		model.addAttribute("mesDesde", dash.getMesDesde());
		model.addAttribute("anioDesde", dash.getAnioDesde());
		model.addAttribute("mesHasta", dash.getMesHasta());
		model.addAttribute("anioHasta", dash.getAnioHasta());
		model.addAttribute("idCategoriaSel", idCategoria);
		model.addAttribute("medioSel", medio);

		model.addAttribute("usuarioLogin", usuarioLogin);

		return "financiero";
	}

	private static Map<Integer, String> mesesDelAnio() {
		Map<Integer, String> meses = new LinkedHashMap<>();
		java.time.Month[] vals = java.time.Month.values();
		for (java.time.Month m : vals) {
			String nombre = m.getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es", "ES"));
			nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1);
			meses.put(m.getValue(), nombre);
		}
		return meses;
	}

	@GetMapping("/export.pdf")
	public ResponseEntity<byte[]> exportPdf(
			@RequestParam(required = false) Integer mesDesde,
			@RequestParam(required = false) Integer anioDesde,
			@RequestParam(required = false) Integer mesHasta,
			@RequestParam(required = false) Integer anioHasta,
			@RequestParam(required = false) Long idCategoria,
			@RequestParam(required = false) MedioPago medio,
			HttpServletRequest request) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return ResponseEntity.badRequest().build();
		}
		Club club = clubService.findById(idClubSession);
		if (club == null) {
			return ResponseEntity.badRequest().build();
		}

		LocalDate hoy = LocalDate.now();
		FinancieroDashboardDTO dash = financieroClubService.obtenerDashboard(idClubSession,
				mesDesde != null ? mesDesde : 1,
				anioDesde != null ? anioDesde : hoy.getYear(),
				mesHasta != null ? mesHasta : 12,
				anioHasta != null ? anioHasta : hoy.getYear());

		List<ReportePagoFilaDTO> filas = financieroClubService.obtenerFilasReporte(idClubSession,
				dash.getMesDesde(), dash.getAnioDesde(), dash.getMesHasta(), dash.getAnioHasta(),
				idCategoria, medio);

		byte[] pdf = financieroExportService.generarPdfReporte(club.getNombre(), dash.getEtiquetaPeriodo(), filas);

		String fname = "reporte-pagos-" + dash.getAnioDesde() + String.format("%02d", dash.getMesDesde()) + "-"
				+ dash.getAnioHasta() + String.format("%02d", dash.getMesHasta()) + ".pdf";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(fname, StandardCharsets.UTF_8).build().toString())
				.contentType(MediaType.APPLICATION_PDF)
				.body(pdf);
	}

	@GetMapping("/export.xlsx")
	public ResponseEntity<byte[]> exportXlsx(
			@RequestParam(required = false) Integer mesDesde,
			@RequestParam(required = false) Integer anioDesde,
			@RequestParam(required = false) Integer mesHasta,
			@RequestParam(required = false) Integer anioHasta,
			@RequestParam(required = false) Long idCategoria,
			@RequestParam(required = false) MedioPago medio,
			HttpServletRequest request) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return ResponseEntity.badRequest().build();
		}
		Club club = clubService.findById(idClubSession);
		if (club == null) {
			return ResponseEntity.badRequest().build();
		}

		LocalDate hoy = LocalDate.now();
		FinancieroDashboardDTO dash = financieroClubService.obtenerDashboard(idClubSession,
				mesDesde != null ? mesDesde : 1,
				anioDesde != null ? anioDesde : hoy.getYear(),
				mesHasta != null ? mesHasta : 12,
				anioHasta != null ? anioHasta : hoy.getYear());

		List<ReportePagoFilaDTO> filas = financieroClubService.obtenerFilasReporte(idClubSession,
				dash.getMesDesde(), dash.getAnioDesde(), dash.getMesHasta(), dash.getAnioHasta(),
				idCategoria, medio);

		byte[] xlsx = financieroExportService.generarExcelReporte(club.getNombre(), dash.getEtiquetaPeriodo(), filas);

		String fname = "reporte-pagos-" + dash.getAnioDesde() + String.format("%02d", dash.getMesDesde()) + "-"
				+ dash.getAnioHasta() + String.format("%02d", dash.getMesHasta()) + ".xlsx";

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(fname, StandardCharsets.UTF_8).build().toString())
				.contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
				.body(xlsx);
	}
}
