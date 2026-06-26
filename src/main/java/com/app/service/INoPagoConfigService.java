package com.app.service;

import java.util.List;

import com.app.dto.NoPagoConfigForm;
import com.app.entity.Deportista;
import com.app.entity.NoPagoConfig;

public interface INoPagoConfigService {

	boolean aplicaNoPago(Long clubId, Deportista deportista, int mes, int anio);

	List<NoPagoConfig> listarPorClub(Long clubId);

	void crear(Long clubId, NoPagoConfigForm form);

	void eliminar(Long id, Long clubId);
}
