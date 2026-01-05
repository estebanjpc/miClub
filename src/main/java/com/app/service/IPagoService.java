package com.app.service;

import java.util.List;

import com.app.dto.MesPagoDTO;
import com.app.entity.OrdenPago;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;

public interface IPagoService {
	
	public List<MesPagoDTO> obtenerMesesParaPagar(Long usuarioId);
//	public void guardarPago(Long deportistaId, int mes, int anio, MedioPago medioPago);
	public List<Pago> obtenerPagosRealizados(Long usuarioId);
	public void registrarPagoEfectivo(List<String> seleccionados);
	public OrdenPago generarOrdenPagoKhipu(List<String> seleccionados, Long usuarioId);
    void confirmarPagoKhipu(String paymentId, String status);
	public void save(Pago p);
	public List<Pago> buscarPagosPorClub(Long idClubSession);
	public void aprobarPagoEfectivo(Long id);
	public List<Pago> buscarPagosFiltrados(Long idClubSession, Integer mes, EstadoPago estado, Long idDeportista);

}
