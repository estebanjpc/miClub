package com.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IDeportistaDao;
import com.app.entity.Deportista;

@Service
public class DeportistaServiceImpl implements IDeportistaService {
	
	@Autowired
	private IDeportistaDao deportistaDao;
	

	@Override
	public List<Deportista> buscarPorClub(Long idClubSession) {
		return deportistaDao.findByClub(idClubSession);
	}

}
