package com.app.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.app.dto.ComprobanteTransferenciaDTO;
import com.app.dto.MesPagoDTO;
import com.app.dto.MorosidadClubDTO;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;

public interface IPagoService {
	
	public List<MesPagoDTO> obtenerMesesParaPagar(Long usuarioId, Long idClub);
//	public void guardarPago(Long deportistaId, int mes, int anio, MedioPago medioPago);
	public List<Pago> obtenerPagosRealizados(Long usuarioId, Long idClub);
	public void registrarPagoEfectivo(List<String> seleccionados, Long usuarioId, Long idClub);

	/** Transferencia con comprobante; queda en PENDIENTE hasta aprobación del club. */
	void registrarPagoTransferencia(List<String> seleccionados, Long usuarioId, Long idClub, MultipartFile comprobante);

	public OrdenPago generarOrdenPagoKhipu(List<String> seleccionados, Long usuarioId, Long idClub);
    void confirmarPagoKhipu(String paymentId, String status);
	public void save(Pago p);
	public List<Pago> buscarPagosPorClub(Long idClubSession);
	public void aprobarPagoEfectivo(Long idPago, Long idClub);
	public List<Pago> buscarPagosFiltrados(Long idClubSession, Integer mes, EstadoPago estado, Long idDeportista);
	public void rechazarYReactivarPago(Long id, String observacion, Long idClub);
	public List<Pago> obtenerPendientesAprobacion(Long idClubSession);
	public List<MorosidadClubDTO> obtenerMorososCriticos(Long idClubSession, Integer mes, Integer anio, int minimoCuotas);

	long contarPagadosEnMes(Long idClub, Integer mes, Integer anio);

	List<Pago> obtenerHistorialDeportistaClub(Long idClub, Long deportistaId);

	/** Apoderado (mismo usuario del deportista) o personal del club (sesión del club). */
	ComprobanteTransferenciaDTO obtenerComprobanteTransferenciaSiAutorizado(Long pagoId, Long usuarioId,
			Long idClubSession, boolean esApoderado);

}
