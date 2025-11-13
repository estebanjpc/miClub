package com.app.service;

import java.util.List;

import com.app.entity.Categoria;
import com.app.entity.Club;

public interface ICategoriaService {

	public List<Categoria> findByClub(Club club);

	public Categoria findById(Long id);

	public void save(Categoria categoria);

	public void delete(Long id);

	public Categoria findByNombreAndClub(String nombre, Club club);
}
