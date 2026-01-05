package com.app.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IPagoDao;
import com.app.dto.DashboardPagoDTO;

@Service
public class DashboardPagoServiceImpl implements IDashboardPagoService {
	
	@Autowired
	private IPagoDao pagoDao;

	@Override
	public DashboardPagoDTO obtenerResumen(Long idClub) {
		int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        Long total = pagoDao.totalDeportistas(idClub);
        Long alDia = pagoDao.deportistasAlDia(idClub, mes, anio);

        DashboardPagoDTO dto = new DashboardPagoDTO();
        dto.setTotalDeportistas(total);
        dto.setAlDia(alDia);
        dto.setMorosos(total - alDia);
        dto.setTotalRecaudadoMes(pagoDao.totalRecaudadoMes(mes, anio));

        return dto;
	}

}
