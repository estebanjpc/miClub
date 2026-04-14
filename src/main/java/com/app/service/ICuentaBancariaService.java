package com.app.service;

import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

public interface ICuentaBancariaService {

	public CuentaBancaria findByClub(Club club);

	/** Cuenta bancaria del club por id (para Khipu webhook sin cargar el club completo). */
	CuentaBancaria findByClubId(Long clubId);

	public void save(CuentaBancaria cuenta);

	public CuentaBancaria findById(Long id);

	public void delete(Long id);

}
