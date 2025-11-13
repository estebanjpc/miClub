package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IClubDao;
import com.app.entity.Club;

@Service
public class ClubServiceImplm implements IClubService {
	
	@Autowired
	private IClubDao clubDao;

	@Override
	public List<Club> findAll() {
		return (List<Club>) clubDao.findAll();
	}

	@Override
	public Club findById(Long id) {
		return clubDao.findById(id).orElse(null);
	}

	@Override
	public void save(Club club) {
		clubDao.save(club);
	}

	@Override
	public boolean existsByCodigo(String codigo) {
		return clubDao.existsByCodigo(codigo);
	}

}
