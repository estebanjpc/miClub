package com.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dto.ConciliacionKhipuFilaDTO;
import com.app.dto.FinancieroDashboardDTO;
import com.app.dto.ReportePagoFilaDTO;
import com.app.entity.Deportista;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;
import com.app.repository.IDeportistaRepository;
import com.app.repository.IOrdenPagoRepository;
import com.app.repository.IPagoRepository;

@Service
public class FinancieroClubServiceImpl implements IFinancieroClubService {

	private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
			.withLocale(new Locale("es", "CL"));

	@Autowired
	private IPagoRepository pagoRepository;

	@Autowired
	private IDeportistaRepository deportistaRepository;

	@Autowired
	private IOrdenPagoRepository ordenPagoRepository;

	@Override
	public FinancieroDashboardDTO obtenerDashboard(Long idClub, Integer mesDesde, Integer anioDesde, Integer mesHasta,
			Integer anioHasta) {
		java.time.LocalDate hoy = java.time.LocalDate.now();
		int md = mesDesde != null ? mesDesde : 1;
		int ad = anioDesde != null ? anioDesde : hoy.getYear();
		int mh = mesHasta != null ? mesHasta : 12;
		int ah = anioHasta != null ? anioHasta : hoy.getYear();

		int desdeK = periodKey(md, ad);
		int hastaK = periodKey(mh, ah);
		if (desdeK > hastaK) {
			int t = md;
			md = mh;
			mh = t;
			t = ad;
			ad = ah;
			ah = t;
			desdeK = periodKey(md, ad);
			hastaK = periodKey(mh, ah);
		}

		FinancieroDashboardDTO dto = new FinancieroDashboardDTO();
		dto.setMesDesde(md);
		dto.setAnioDesde(ad);
		dto.setMesHasta(mh);
		dto.setAnioHasta(ah);
		dto.setEtiquetaPeriodo(etiquetaRango(md, ad, mh, ah));

		int desde = desdeK;
		int hasta = hastaK;

		Long rec = pagoRepository.totalRecaudadoRango(idClub, desde, hasta);
		dto.setTotalRecaudado(rec != null ? rec : 0L);

		dto.setDeudaTotal(calcularDeudaTotal(idClub, mh, ah));

		long totalActivos = deportistaRepository.countActivosHastaMes(idClub, LocalDate.of(ah, mh, 1));
		Long alDia = pagoRepository.deportistasAlDia(idClub, mh, ah);
		long alDiaL = alDia != null ? alDia : 0L;
		dto.setCantidadMorosos(Math.max(0L, totalActivos - alDiaL));

		return dto;
	}

	private long calcularDeudaTotal(Long idClub, int mesRef, int anioRef) {
		List<Deportista> deportistas = deportistaRepository.findByClub(idClub);
		List<Pago> pagos = pagoRepository.obtenerEstadoAcumulado(idClub, mesRef, anioRef);

		Map<String, Pago> ultimoPagoPorPeriodo = new HashMap<>();
		for (Pago pago : pagos) {
			String periodoKey = buildPeriodoKey(pago.getDeportista().getId(), pago.getAnio(), pago.getMes());
			ultimoPagoPorPeriodo.putIfAbsent(periodoKey, pago);
		}

		YearMonth periodoConsulta = YearMonth.of(anioRef, mesRef);
		long deuda = 0L;

		for (Deportista deportista : deportistas) {
			if (deportista.getFechaIngreso() == null || deportista.getCategoria() == null) {
				continue;
			}
			int valor = deportista.getCategoria().getValorCuota();
			YearMonth inicio = YearMonth.from(deportista.getFechaIngreso());
			if (inicio.isAfter(periodoConsulta)) {
				continue;
			}
			YearMonth cursor = inicio;
			while (!cursor.isAfter(periodoConsulta)) {
				String key = buildPeriodoKey(deportista.getId(), cursor.getYear(), cursor.getMonthValue());
				Pago pagoPeriodo = ultimoPagoPorPeriodo.get(key);
				if (pagoPeriodo == null || pagoPeriodo.getEstado() != EstadoPago.PAGADO) {
					deuda += valor;
				}
				cursor = cursor.plusMonths(1);
			}
		}
		return deuda;
	}

	private static String buildPeriodoKey(Long deportistaId, Integer anio, Integer mes) {
		return deportistaId + "-" + anio + "-" + mes;
	}

	private static int periodKey(int mes, int anio) {
		return anio * 100 + mes;
	}

	private static String etiquetaRango(int m1, int a1, int m2, int a2) {
		String i = mesNombre(m1) + " " + a1;
		String f = mesNombre(m2) + " " + a2;
		return i + " – " + f;
	}

	private static String mesNombre(int mes) {
		String n = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
		if (n == null || n.isEmpty()) {
			return "";
		}
		return n.substring(0, 1).toUpperCase() + n.substring(1);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReportePagoFilaDTO> obtenerFilasReporte(Long idClub, int mesDesde, int anioDesde, int mesHasta,
			int anioHasta, Long idCategoria, MedioPago medio) {
		int desde = periodKey(mesDesde, anioDesde);
		int hasta = periodKey(mesHasta, anioHasta);
		if (desde > hasta) {
			int tmp = desde;
			desde = hasta;
			hasta = tmp;
		}

		List<Pago> lista = pagoRepository.findByClubRangoPeriodo(idClub, desde, hasta);
		List<ReportePagoFilaDTO> filas = new ArrayList<>();

		for (Pago p : lista) {
			if (idCategoria != null && (p.getDeportista().getCategoria() == null
					|| !idCategoria.equals(p.getDeportista().getCategoria().getId()))) {
				continue;
			}
			if (medio != null && p.getMedioPago() != medio) {
				continue;
			}
			filas.add(mapearFila(p));
		}
		return filas;
	}

	private ReportePagoFilaDTO mapearFila(Pago p) {
		ReportePagoFilaDTO dto = new ReportePagoFilaDTO();
		dto.setDeportista(p.getDeportista().getNombre() + " " + p.getDeportista().getApellido());
		dto.setCategoria(p.getDeportista().getCategoria() != null ? p.getDeportista().getCategoria().getNombre() : "");
		dto.setPeriodoCuota(p.getNombreMes() + " " + p.getAnio());
		dto.setEstado(p.getEstado() != null ? p.getEstado().name() : "");
		dto.setMedio(p.getMedioPago() != null ? p.getMedioPago().name() : "");
		int valor = p.getDeportista().getCategoria() != null ? p.getDeportista().getCategoria().getValorCuota() : 0;
		dto.setMonto(valor);
		LocalDateTime f = p.getFecha();
		dto.setFechaRegistro(f != null ? f.format(FMT_FECHA) : "");
		return dto;
	}

	@Override
	@Transactional(readOnly = true)
	public List<ConciliacionKhipuFilaDTO> obtenerConciliacionKhipu(Long idClub) {
		List<OrdenPago> ordenes = ordenPagoRepository.findByClubIdOrderByFechaCreacionDesc(idClub);
		List<ConciliacionKhipuFilaDTO> salida = new ArrayList<>();

		for (OrdenPago o : ordenes) {
			if (o.getMedioPago() != MedioPago.KHIPU) {
				continue;
			}
			ConciliacionKhipuFilaDTO row = new ConciliacionKhipuFilaDTO();
			row.setIdOrden(o.getId());
			row.setFechaCreacion(o.getFechaCreacion());
			row.setMontoTotal(o.getMontoTotal());
			row.setKhipuPaymentId(o.getKhipuPaymentId());
			row.setEstadoOrden(o.getEstado() != null ? o.getEstado().name() : "");

			List<Pago> pagos = o.getPagos();
			int n = pagos != null ? pagos.size() : 0;
			row.setCantidadPagos(n);

			long montoPag = 0;
			int pag = 0;
			int otros = 0;
			if (pagos != null) {
				for (Pago p : pagos) {
					if (p.getEstado() == EstadoPago.PAGADO) {
						pag++;
						int v = p.getDeportista().getCategoria() != null
								? p.getDeportista().getCategoria().getValorCuota()
								: 0;
						montoPag += v;
					} else {
						otros++;
					}
				}
			}
			row.setMontoItemsPagados(montoPag);
			row.setItemsPagados(pag);
			row.setItemsPendientesOtro(otros);
			salida.add(row);
		}
		return salida;
	}
}
