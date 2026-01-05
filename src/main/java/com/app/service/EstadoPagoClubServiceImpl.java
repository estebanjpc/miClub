package com.app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IDeportistaDao;
import com.app.dao.IPagoDao;
import com.app.dto.EstadoPagoDeportistaDTO;
import com.app.entity.Deportista;
import com.app.entity.Pago;
import com.app.enums.EstadoPago;

@Service
public class EstadoPagoClubServiceImpl implements IEstadoPagoClubService {
	
	@Autowired
	private IDeportistaDao deportistaDao;
	
	@Autowired
	private IPagoDao pagoDao;

	@Override
	public List<EstadoPagoDeportistaDTO> obtenerEstadoPorMes(Long idClub, Integer mes, Integer anio) {
		List<Deportista> deportistas = deportistaDao.findByClub(idClub);

	        List<Pago> pagos = pagoDao.findByClubMesAnio(idClub, mes, anio);

	        Map<Long, Pago> pagosPorDeportista = pagos.stream()
	            .collect(Collectors.toMap(
	                p -> p.getDeportista().getId(),
	                p -> p
	            ));

	        List<EstadoPagoDeportistaDTO> resultado = new ArrayList<>();

	        for (Deportista d : deportistas) {

	            Pago pago = pagosPorDeportista.get(d.getId());

	            EstadoPagoDeportistaDTO dto = new EstadoPagoDeportistaDTO();
	            dto.setIdDeportista(d.getId());
	            dto.setNombreCompleto(d.getNombre() + " " + d.getApellido());
	            dto.setMes(mes);
	            dto.setAnio(anio);

	            if (pago != null) {
	                dto.setEstado(pago.getEstado());
	                dto.setMedioPago(pago.getMedioPago());
	                dto.setIdPago(pago.getId());
	                dto.setFechaPago(pago.getFecha());
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
