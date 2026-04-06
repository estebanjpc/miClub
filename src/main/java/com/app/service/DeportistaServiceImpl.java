package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.IDeportistaRepository;
import com.app.entity.Deportista;

@Service
public class DeportistaServiceImpl implements IDeportistaService {
	
	@Autowired
	private IDeportistaRepository deportistaRepository;
	

	@Override
	public List<Deportista> buscarPorClub(Long idClubSession) {
		return deportistaRepository.findByClub(idClubSession);
	}

	@Override
	public List<Deportista> listarTodosPorClub(Long idClub) {
		return deportistaRepository.findAllByClubWithUsuarioAndCategoria(idClub);
	}

	@Override
	public List<Deportista> listarPorUsuario(Long usuarioId) {
		return deportistaRepository.findByUsuarioIdOrderById(usuarioId);
	}

	@Override
	public Deportista findById(Long id) {
		return deportistaRepository.findById(id).orElse(null);
	}

	@Override
	public void save(Deportista deportista) {
		deportistaRepository.save(deportista);
	}

}
