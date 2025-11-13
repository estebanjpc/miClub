package com.app.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.app.entity.Banco;

public interface IBancoDao extends CrudRepository<Banco, Long> {

	Banco findByNombre(String nombre);
	Optional<Banco> findById(Long id);
	
}
