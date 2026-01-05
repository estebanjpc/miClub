package com.app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dao.IDeportistaDao;
import com.app.dao.IOrdenPagoDao;
import com.app.dao.IPagoDao;
import com.app.dto.KhipuResponse;
import com.app.dto.MesPagoDTO;
import com.app.entity.Deportista;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

@Service
public class PagoServiceImpl implements IPagoService {
	
	@Autowired
	private IDeportistaDao deportistaDao;
	
	@Autowired
	private IPagoDao pagoDao;
	
	@Autowired
	private IOrdenPagoDao ordenPagoDao;
	
	@Autowired
	private IKhipuService khipuService;

	@Override
	public List<MesPagoDTO> obtenerMesesParaPagar(Long usuarioId) {

		List<Deportista> deportistas = deportistaDao.findByUsuarioId(usuarioId);
		List<MesPagoDTO> lista = new ArrayList<>();

		LocalDate hoy = LocalDate.now();
		int anioActual = hoy.getYear();
		int mesActual = hoy.getMonthValue();

		for (Deportista d : deportistas) {

			int valorCuota = d.getCategoria().getValorCuota();

			// recorrido de meses desde mesActual hasta diciembre
			for (int mes = mesActual; mes <= 12; mes++) {

				boolean existePago = pagoDao.findByDeportistaIdAndMesAndAnio(d.getId(), mes, anioActual).isPresent();

				if (!existePago) {
					lista.add(new MesPagoDTO(d.getId(), d.getNombre() + " " + d.getApellido(), mes, anioActual,
							valorCuota));
				}
			}
		}

		return lista;
	}

	@Override
	@Transactional
	public List<Pago> obtenerPagosRealizados(Long usuarioId) {

		List<Deportista> deportistas = deportistaDao.findByUsuarioId(usuarioId);
		List<Pago> lista = new ArrayList<>();

		for (Deportista d : deportistas) {
			List<Pago> pagos = pagoDao.findByDeportistaId(d.getId());
			lista.addAll(pagos);
		}

		return lista;
	}

	@Override
	public void registrarPagoEfectivo(List<String> seleccionados) {
		for (String item : seleccionados) {
            String[] data = item.split("-");
            Long deportistaId = Long.parseLong(data[0]);
            int mes = Integer.parseInt(data[1]);
            int anio = Integer.parseInt(data[2]);

            Deportista d = deportistaDao.findById(deportistaId).orElseThrow();
            Pago pago = new Pago();
            pago.setDeportista(d);
            pago.setClub(d.getCategoria().getClub());
            pago.setFecha(LocalDateTime.now());
            pago.setMes(mes);
            pago.setAnio(anio);
            pago.setEstado(EstadoPago.PENDIENTE);
            pago.setMedioPago(MedioPago.EFECTIVO);
            pago.setObservacion("Pago registrado desde portal MiClub (efectivo)");
            pagoDao.save(pago);
        }
	}

	@Override
	@Transactional
	public OrdenPago generarOrdenPagoKhipu(List<String> seleccionados, Long idUsuario) {
		List<Pago> pagos = new ArrayList<>();
        int total = 0;

        for (String item : seleccionados) {
            String[] d = item.split("-");
            Long deportistaId = Long.parseLong(d[0]);
            int mes = Integer.parseInt(d[1]);
            int anio = Integer.parseInt(d[2]);

            Deportista dep = deportistaDao.findById(deportistaId).orElseThrow();

            Pago p = new Pago();
            p.setDeportista(dep);
            p.setClub(dep.getCategoria().getClub());
            p.setFecha(LocalDateTime.now());
            p.setMes(mes);
            p.setAnio(anio);
            p.setEstado(EstadoPago.PENDIENTE_KHIPU);
            p.setMedioPago(MedioPago.KHIPU);
            p.setObservacion("Pago vía Khipu iniciado");
            pagoDao.save(p);

            pagos.add(p);

            // calcular valor por deportista: asumo m.getValorCuota o similar
            // si no tienes el valor en Deportista, reemplaza con m.valorCuota pasado desde front
            Integer valor = dep.getCategoria().getValorCuota(); // ajusta segun tu modelo
            total += (valor != null ? valor : 0);
        }

        OrdenPago orden = new OrdenPago();
        orden.setIdUsuario(idUsuario);
        orden.setMontoTotal(total);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setMedioPago(MedioPago.KHIPU);
        orden.setPagos(pagos);

        OrdenPago saved = ordenPagoDao.save(orden);

        // llamar a Khipu
        KhipuResponse resp = khipuService.crearPago(total, pagos, saved.getId());

        saved.setKhipuPaymentId(resp.getPaymentId());
        saved.setKhipuUrl(resp.getPaymentUrl());

        return ordenPagoDao.save(saved);
	}
	
	@Override
    @Transactional
    public void confirmarPagoKhipu(String paymentId, String status) {
        OrdenPago orden = ordenPagoDao.findByKhipuPaymentId(paymentId);
        if (orden == null) return;

        if ("done".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
            orden.getPagos().forEach(p -> p.setEstado(EstadoPago.PAGADO));
            orden.getPagos().forEach(pagoDao::save);
            // opcional: guardar observación
        } else {
            orden.getPagos().forEach(p -> p.setEstado(EstadoPago.RECHAZADO));
            orden.getPagos().forEach(pagoDao::save);
        }
    }

	@Override
	public void save(Pago p) {
		pagoDao.save(p);		
	}

	@Override
	public List<Pago> buscarPagosPorClub(Long idClubSession) {
		return pagoDao.findByClubIdOrderByFechaDesc(idClubSession);
	}
	
	@Transactional
    public void aprobarPagoEfectivo(Long idPago) {
        Pago pago = pagoDao.findById(idPago).orElseThrow(() -> new RuntimeException("Pago no encontrado"));

        pago.setEstado(EstadoPago.PAGADO);
        pago.setFecha(LocalDateTime.now());

        pagoDao.save(pago);
    }

	@Override
	public List<Pago> buscarPagosFiltrados(Long idClubSession, Integer mes, EstadoPago estado, Long idDeportista) {
		return pagoDao.buscarPagosFiltrados(idClubSession,mes,estado,idDeportista);
	}

}
