package com.app.service;

import java.util.List;

import com.app.entity.Club;

public interface IClubService {

	public List<Club> findAll();
	public Club findById(Long id);
	public void save(Club club);
	public boolean existsByCodigo(String uniqueSlug);
	}
