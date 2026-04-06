package com.app.service;

import java.util.List;

import com.app.entity.Club;
import com.app.entity.Usuario;

public interface IClubService {

	public List<Club> findAll();
	public Club findById(Long id);
	public void save(Club club, Usuario usuarioLogin);
	public boolean existsByCodigo(String uniqueSlug);
	}
