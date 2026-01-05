package com.app.dao;

import org.springframework.data.repository.CrudRepository;

import com.app.entity.OrdenPago;

public interface IOrdenPagoDao  extends CrudRepository<OrdenPago, Long> {

	OrdenPago findByKhipuPaymentId(String khipuPaymentId);
	
}
