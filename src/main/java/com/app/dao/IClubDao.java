package com.app.dao;

import org.springframework.data.repository.CrudRepository;

import com.app.entity.Club;

public interface IClubDao extends CrudRepository<Club, Long> {

	public boolean existsByCodigo(String codigo);
	
}
