package com.app.service;

import com.app.dto.DashboardPagoDTO;

public interface IDashboardPagoService {

	/**
	 * @param mes  mes del período (1–12); si es null se usa el mes actual
	 * @param anio año del período; si es null se usa el año actual
	 */
	DashboardPagoDTO obtenerResumen(Long idClub, Integer mes, Integer anio);

}
