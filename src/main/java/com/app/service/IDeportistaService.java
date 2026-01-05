package com.app.service;

import java.util.List;

import com.app.entity.Deportista;

public interface IDeportistaService {

	List<Deportista> buscarPorClub(Long idClubSession);

}
