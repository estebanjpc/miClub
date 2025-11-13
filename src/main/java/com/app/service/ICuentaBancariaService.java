package com.app.service;

import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

public interface ICuentaBancariaService {

	public CuentaBancaria findByClub(Club club);

	public void save(CuentaBancaria cuenta);

	public CuentaBancaria findById(Long id);

	public void delete(Long id);

}
