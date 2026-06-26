package com.app.notification.service;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.Usuario;
import com.app.notification.dto.MassEmailFilter;
import com.app.notification.dto.MassEmailRequest;
import com.app.repository.IPagoRepository;
import com.app.service.AsyncEmailService;
import com.app.service.IClubService;
import com.app.service.IDeportistaService;
import com.app.service.INoPagoConfigService;

@Service
public class MassEmailService {

	private final IClubService clubService;
	private final IDeportistaService deportistaService;
	private final IPagoRepository pagoRepository;
	private final AsyncEmailService asyncEmailService;
	private final INoPagoConfigService noPagoConfigService;

	public MassEmailService(IClubService clubService, IDeportistaService deportistaService,
			IPagoRepository pagoRepository, AsyncEmailService asyncEmailService, INoPagoConfigService noPagoConfigService) {
		this.clubService = clubService;
		this.deportistaService = deportistaService;
		this.pagoRepository = pagoRepository;
		this.asyncEmailService = asyncEmailService;
		this.noPagoConfigService = noPagoConfigService;
	}

	/**
	 * Resuelve destinatarios y encola envíos asíncronos (un correo por email único).
	 *
	 * @return cantidad de destinatarios únicos encolados
	 */
	public int enviar(Long clubId, MassEmailRequest req) {
		if (!StringUtils.hasText(req.getSubject())) {
			throw new IllegalArgumentException("El asunto es obligatorio");
		}
		if (!StringUtils.hasText(req.getMessage())) {
			throw new IllegalArgumentException("El mensaje es obligatorio");
		}
		Club club = clubService.findById(clubId);
		if (club == null) {
			throw new IllegalArgumentException("Club no encontrado");
		}

		MassEmailFilter filter = req.getFilter() != null ? req.getFilter() : MassEmailFilter.ALL;
		List<Deportista> destinatarios = resolverDestinatarios(clubId, req, filter);

		LinkedHashSet<String> emailsVistos = new LinkedHashSet<>();
		int encolados = 0;
		for (Deportista d : destinatarios) {
			Usuario u = d.getUsuario();
			if (u == null || !StringUtils.hasText(u.getEmail())) {
				continue;
			}
			String key = u.getEmail().trim().toLowerCase();
			if (!emailsVistos.add(key)) {
				continue;
			}
			asyncEmailService.enviarNotificacionClub(u.getEmail().trim(), req.getSubject(), req.getMessage(),
					club.getNombre(), nombreCompletoUsuario(u), clubId);
			encolados++;
		}
		return encolados;
	}

	public long contarMorosos(Long clubId, int mes, int anio) {
		return listarMorosos(clubId, mes, anio).size();
	}

	/** Deportistas sin pago bloqueante en el mes/año (morosos del período). */
	public List<Deportista> listarMorosos(Long clubId, int mes, int anio) {
		YearMonth periodo = YearMonth.of(anio, mes);
		return deportistaService.listarTodosPorClub(clubId).stream()
				.filter(d -> debeEvaluarPeriodo(d, periodo))
				.filter(d -> !noPagoConfigService.aplicaNoPago(clubId, d, mes, anio))
				.filter(d -> d.getId() != null && !pagoRepository.existsPagoBloqueante(d.getId(), mes, anio))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	private List<Deportista> resolverDestinatarios(Long clubId, MassEmailRequest req, MassEmailFilter filter) {
		List<Deportista> base = deportistaService.listarTodosPorClub(clubId);
		switch (filter) {
		case ALL:
			return new ArrayList<>(base);
		case CATEGORY:
			if (req.getCategoryIds() == null || req.getCategoryIds().isEmpty()) {
				throw new IllegalArgumentException("categoryIds es obligatorio para filter=CATEGORY");
			}
			Set<Long> setCategorias = req.getCategoryIds().stream().filter(Objects::nonNull).collect(Collectors.toSet());
			return base.stream()
					.filter(d -> d.getCategoria() != null && setCategorias.contains(d.getCategoria().getId()))
					.collect(Collectors.toCollection(ArrayList::new));
		case CUSTOM:
			if (req.getSelectedDeportistaIds() == null || req.getSelectedDeportistaIds().isEmpty()) {
				throw new IllegalArgumentException("selectedUserIds (deportistas) es obligatorio para filter=CUSTOM");
			}
			Set<Long> ids = new LinkedHashSet<>(req.getSelectedDeportistaIds());
			return base.stream().filter(d -> d.getId() != null && ids.contains(d.getId()))
					.collect(Collectors.toCollection(ArrayList::new));
		case DEBTORS:
			int mes = req.getMonth() != null ? req.getMonth() : YearMonth.now().getMonthValue();
			int anio = req.getYear() != null ? req.getYear() : YearMonth.now().getYear();
			if (mes < 1 || mes > 12) {
				throw new IllegalArgumentException("month debe estar entre 1 y 12");
			}
			final int fm = mes;
			final int fa = anio;
			YearMonth periodo = YearMonth.of(fa, fm);
			List<Deportista> morosos = base.stream()
					.filter(d -> debeEvaluarPeriodo(d, periodo))
					.filter(d -> !noPagoConfigService.aplicaNoPago(clubId, d, fm, fa))
					.filter(d -> d.getId() != null && !pagoRepository.existsPagoBloqueante(d.getId(), fm, fa))
					.collect(Collectors.toCollection(ArrayList::new));
			if (req.getSelectedDeportistaIds() != null && !req.getSelectedDeportistaIds().isEmpty()) {
				Set<Long> sel = new LinkedHashSet<>(req.getSelectedDeportistaIds());
				return morosos.stream().filter(d -> sel.contains(d.getId())).collect(Collectors.toCollection(ArrayList::new));
			}
			return morosos;
		default:
			return new ArrayList<>(base);
		}
	}

	/** No exige cuota de meses anteriores al ingreso del deportista. */
	private static boolean debeEvaluarPeriodo(Deportista d, YearMonth periodo) {
		if (d.getFechaIngreso() == null) {
			return true;
		}
		return !YearMonth.from(d.getFechaIngreso()).isAfter(periodo);
	}

	private static String nombreCompletoUsuario(Usuario u) {
		String nombre = u.getNombre() != null ? u.getNombre().trim() : "";
		String apellido = u.getApellido() != null ? u.getApellido().trim() : "";
		return (nombre + " " + apellido).trim();
	}
}
