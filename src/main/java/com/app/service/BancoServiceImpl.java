package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IBancoDao;
import com.app.entity.Banco;

@Service
public class BancoServiceImpl implements IBancoService {
	
	@Autowired
	private IBancoDao bancoDao;

	@Override
	public List<Banco> findAll() {
		return (List<Banco>) bancoDao.findAll();
	}

	@Override
	public Banco findByNombre(String nombre) {
		return bancoDao.findByNombre(nombre);
	}

	@Override
	public Banco findById(Long id) {
		return bancoDao.findById(id).orElse(null);
	}

}
