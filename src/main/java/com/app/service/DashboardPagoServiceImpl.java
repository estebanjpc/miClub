package com.app.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.IDeportistaRepository;
import com.app.repository.IPagoRepository;
import com.app.dto.DashboardPagoDTO;

@Service
public class DashboardPagoServiceImpl implements IDashboardPagoService {
	
	@Autowired
	private IPagoRepository pagoRepository;
	
	@Autowired
	private IDeportistaRepository deportistaRepository;

	@Override
	public DashboardPagoDTO obtenerResumen(Long idClub, Integer mes, Integer anio) {
		LocalDate hoy = LocalDate.now();
		int m = mes != null ? mes : hoy.getMonthValue();
		int a = anio != null ? anio : hoy.getYear();

		Long total = deportistaRepository.countActivosHastaMes(idClub, m, a);
		Long alDia = pagoRepository.deportistasAlDia(idClub, m, a);

		DashboardPagoDTO dto = new DashboardPagoDTO();
		dto.setTotalDeportistas(total);
		dto.setAlDia(alDia);
		// Morosos del período: activos que no tienen ese mes/año en PAGADO (incluye sin registro o con otro estado).
		long morosos = total - alDia;
		dto.setMorosos(Math.max(0L, morosos));
		Long recaudado = pagoRepository.totalRecaudadoEnMes(idClub, m, a);
		dto.setTotalRecaudadoMes(recaudado != null ? recaudado.intValue() : 0);

		return dto;
	}

}
