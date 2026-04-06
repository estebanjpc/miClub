package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.ICategoriaRepository;
import com.app.entity.Categoria;
import com.app.entity.Club;

@Service
public class CategoriaServiceImpl implements ICategoriaService {
	
	@Autowired
	private ICategoriaRepository categoriaRepository;

	@Override
	public List<Categoria> findByClub(Club club) {
		return categoriaRepository.findByClub(club);
	}

	@Override
	public Categoria findById(Long id) {
		return categoriaRepository.findById(id).orElse(null);
	}

	@Override
	public void save(Categoria categoria) {
		categoriaRepository.save(categoria);
	}

	@Override
	public void delete(Long id) {
		categoriaRepository.deleteById(id);
	}

	@Override
	public Categoria findByNombreAndClub(String nombre, Club club) {
	    return categoriaRepository.findByNombreAndClub(nombre, club);
	}
}
