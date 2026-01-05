package com.app.service;

import java.util.List;

import com.app.dto.KhipuResponse;
import com.app.entity.Pago;

public interface IKhipuService {

	KhipuResponse crearPago(Integer montoEnPesos, List<Pago> pagos, Long ordenId);
	
}
