package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.IBancoRepository;
import com.app.entity.Banco;

@Service
public class BancoServiceImpl implements IBancoService {
	
	@Autowired
	private IBancoRepository bancoRepository;

	@Override
	public List<Banco> findAll() {
		return (List<Banco>) bancoRepository.findAll();
	}

	@Override
	public Banco findByNombre(String nombre) {
		return bancoRepository.findByNombre(nombre);
	}

	@Override
	public Banco findById(Long id) {
		return bancoRepository.findById(id).orElse(null);
	}

}
