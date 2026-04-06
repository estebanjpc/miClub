package com.app.service;

import java.util.List;

import com.app.entity.Deportista;

public interface IDeportistaService {

	List<Deportista> buscarPorClub(Long idClubSession);

	/** Todos los deportistas del club (activos e inactivos), con categoría y usuario cargados. */
	List<Deportista> listarTodosPorClub(Long idClub);

	/** Deportistas del usuario (apoderado), con categoría cargada, ordenados por id. */
	List<Deportista> listarPorUsuario(Long usuarioId);

	Deportista findById(Long id);

	void save(Deportista deportista);

}
