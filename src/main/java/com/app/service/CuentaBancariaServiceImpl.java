package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.ICuentaBancariaDao;
import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

@Service
public class CuentaBancariaServiceImpl implements ICuentaBancariaService {
	
	@Autowired
	private ICuentaBancariaDao cuentaBancariaDao;

	@Override
	public CuentaBancaria findByClub(Club club) {
		return cuentaBancariaDao.findByClub(club);
	}

	@Override
	public void save(CuentaBancaria cuenta) {
		cuentaBancariaDao.save(cuenta);
	}

	@Override
	public CuentaBancaria findById(Long id) {
		return cuentaBancariaDao.findById(id).orElse(null);
	}

	@Override
	public void delete(Long id) {
		cuentaBancariaDao.deleteById(id);
	}

}
