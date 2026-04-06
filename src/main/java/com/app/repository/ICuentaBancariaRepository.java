package com.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Club;
import com.app.entity.CuentaBancaria;

public interface ICuentaBancariaRepository extends JpaRepository<CuentaBancaria, Long> {

	CuentaBancaria findByClub(Club club);

}

