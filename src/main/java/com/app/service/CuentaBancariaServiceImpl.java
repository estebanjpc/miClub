package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.ICuentaBancariaRepository;
import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

@Service
public class CuentaBancariaServiceImpl implements ICuentaBancariaService {
	
	@Autowired
	private ICuentaBancariaRepository cuentaBancariaRepository;

	@Override
	public CuentaBancaria findByClub(Club club) {
		return cuentaBancariaRepository.findByClub(club);
	}

	@Override
	public void save(CuentaBancaria cuenta) {
		cuentaBancariaRepository.save(cuenta);
	}

	@Override
	public CuentaBancaria findById(Long id) {
		return cuentaBancariaRepository.findById(id).orElse(null);
	}

	@Override
	public void delete(Long id) {
		cuentaBancariaRepository.deleteById(id);
	}

}
