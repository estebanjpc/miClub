package com.app.service;

import java.util.Optional;

import com.app.entity.OrdenPago;

public interface IOrdenPagoService {

	public OrdenPago findByKhipuPaymentId(String paymentId);

	public void save(OrdenPago orden);

	public OrdenPago buscarPorId(Long id);

	Optional<OrdenPago> buscarPorIdOptional(Long id);

}
