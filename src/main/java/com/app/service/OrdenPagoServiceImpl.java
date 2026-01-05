package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.dao.IOrdenPagoDao;
import com.app.entity.OrdenPago;

@Service
public class OrdenPagoServiceImpl implements IOrdenPagoService {
	
	@Autowired
	private IOrdenPagoDao ordenPagoDao;

	@Override
	public OrdenPago findByKhipuPaymentId(String paymentId) {
		return ordenPagoDao.findByKhipuPaymentId(paymentId);
	}

	@Override
	public void save(OrdenPago orden) {
		ordenPagoDao.save(orden);		
	}

	@Override
	public OrdenPago buscarPorId(Long id) {
		return ordenPagoDao.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));
	}

}
