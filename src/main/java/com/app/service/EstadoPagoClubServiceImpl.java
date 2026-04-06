package com.app.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.IDeportistaRepository;
import com.app.repository.IPagoRepository;
import com.app.dto.EstadoPagoDeportistaDTO;
import com.app.entity.Deportista;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;

@Service
public class EstadoPagoClubServiceImpl implements IEstadoPagoClubService {
	
	@Autowired
	private IDeportistaRepository deportistaRepository;
	
	@Autowired
	private IPagoRepository pagoRepository;

	@Override
	public List<EstadoPagoDeportistaDTO> obtenerEstadoPorMes(Long idClub, Integer mes, Integer anio,String tipo) {
		List<Deportista> deportistas = deportistaRepository.findByClub(idClub);

		List<Pago> pagos = null;    
		
		if(tipo.equalsIgnoreCase("MES"))
			pagos = pagoRepository.findByClubMesAnio(idClub, mes, anio);
		else pagos = pagoRepository.obtenerEstadoAcumulado(idClub, mes, anio);

	        Map<Long, Pago> pagosPorDeportista = pagos.stream()
	            .collect(Collectors.toMap(
	                p -> p.getDeportista().getId(),
	                p -> p,
	                (p1, p2) -> {
	                	// Si hay más de un pago para el mismo deportista, conservar el más reciente.
	                	if (p1.getFecha() == null) {
	                		return p2;
	                	}
	                	if (p2.getFecha() == null) {
	                		return p1;
	                	}
	                	return p1.getFecha().isAfter(p2.getFecha()) ? p1 : p2;
	                }
	            ));

	        List<EstadoPagoDeportistaDTO> resultado = new ArrayList<>();

	        for (Deportista d : deportistas) {

	        	LocalDate fechaIngreso = d.getFechaIngreso();
	            LocalDate fechaConsulta = LocalDate.of(anio, mes, 1);

	            if (fechaIngreso != null && fechaIngreso.isAfter(fechaConsulta.withDayOfMonth(1))) {
	                continue; // no existía aún → no debe aparecer
	            }

	            Pago pago = pagosPorDeportista.get(d.getId());

	            EstadoPagoDeportistaDTO dto = new EstadoPagoDeportistaDTO();
	            dto.setIdDeportista(d.getId());
	            dto.setNombreCompleto(d.getNombre() + " " + d.getApellido());
	            dto.setMes(mes);
	            dto.setAnio(anio);
	            if (d.getCategoria() != null) {
	            	dto.setIdCategoria(d.getCategoria().getId());
	            	dto.setNombreCategoria(d.getCategoria().getNombre());
	            }

	            if (pago != null) {
	                dto.setEstado(pago.getEstado());
	                dto.setMedioPago(pago.getMedioPago());
	                dto.setIdPago(pago.getId());
	                dto.setFechaPago(pago.getFecha());
	                dto.setObservacion(pago.getObservacion());
	                if (pago.getOrdenPago() != null) {
	                    dto.setIdOrdenPago(pago.getOrdenPago().getId());
	                }
	            } else {
	                dto.setEstado(EstadoPago.MOROSO);
	            }

	            resultado.add(dto);
	        }

	        return resultado;
	}

}
