package com.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Banco;

public interface IBancoRepository extends JpaRepository<Banco, Long> {

	Banco findByNombre(String nombre);

	Optional<Banco> findById(Long id);

}

