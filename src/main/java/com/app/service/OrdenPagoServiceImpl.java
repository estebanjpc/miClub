package com.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.repository.IOrdenPagoRepository;
import com.app.entity.OrdenPago;

@Service
public class OrdenPagoServiceImpl implements IOrdenPagoService {
	
	@Autowired
	private IOrdenPagoRepository ordenPagoRepository;

	@Override
	public OrdenPago findByKhipuPaymentId(String paymentId) {
		return ordenPagoRepository.findByKhipuPaymentId(paymentId);
	}

	@Override
	public void save(OrdenPago orden) {
		ordenPagoRepository.save(orden);		
	}

	@Override
	public OrdenPago buscarPorId(Long id) {
		return ordenPagoRepository.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada"));
	}

}
