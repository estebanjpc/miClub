package com.app.service;

import java.util.List;

import com.app.dto.EstadoPagoDeportistaDTO;

public interface IEstadoPagoClubService {

	public List<EstadoPagoDeportistaDTO> obtenerEstadoPorMes(Long idClub, Integer mes, Integer anio);
	
}
