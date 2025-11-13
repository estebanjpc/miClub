package com.app.service;

import java.util.List;

import com.app.entity.Banco;

public interface IBancoService {

	public List<Banco> findAll();

	public Banco findByNombre(String nombre);

	public Banco findById(Long id);

}
