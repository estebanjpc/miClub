package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.ICategoriaDao;
import com.app.entity.Categoria;
import com.app.entity.Club;

@Service
public class CategoriaServiceImpl implements ICategoriaService {
	
	@Autowired
	private ICategoriaDao categoriaDao;

	@Override
	public List<Categoria> findByClub(Club club) {
		return categoriaDao.findByClub(club);
	}

	@Override
	public Categoria findById(Long id) {
		return categoriaDao.findById(id).orElse(null);
	}

	@Override
	public void save(Categoria categoria) {
		categoriaDao.save(categoria);
	}

	@Override
	public void delete(Long id) {
		categoriaDao.deleteById(id);
	}

	@Override
	public Categoria findByNombreAndClub(String nombre, Club club) {
	    return categoriaDao.findByNombreAndClub(nombre, club);
	}
}
