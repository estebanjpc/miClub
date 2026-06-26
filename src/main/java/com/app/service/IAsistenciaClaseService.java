package com.app.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.app.dto.AsistenciaEstadisticaDTO;

public interface IAsistenciaClaseService {

	void registrarAsistenciaDia(Long clubId, Long entrenadorId, LocalDate fechaClase, Set<Long> presentesIds);

	Set<Long> obtenerPresentesEnFecha(Long clubId, LocalDate fechaClase);

	List<AsistenciaEstadisticaDTO> estadisticasClub(Long clubId, LocalDate desde, LocalDate hasta);

	List<AsistenciaEstadisticaDTO> estadisticasUsuario(Long clubId, Long usuarioId, LocalDate desde, LocalDate hasta);
}
