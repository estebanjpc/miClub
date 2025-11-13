package com.app.dao;

import org.springframework.data.repository.CrudRepository;

import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

public interface ICuentaBancariaDao extends CrudRepository<CuentaBancaria, Long> {

	CuentaBancaria findByClub(Club club);
	
}
