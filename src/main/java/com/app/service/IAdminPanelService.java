package com.app.service;

import java.util.List;

import com.app.dto.ClubListadoAdminItem;
import com.app.entity.Club;
import com.app.entity.Pago;

public interface IAdminPanelService {

	List<ClubListadoAdminItem> listarClubesConConteos();

	/** Club por id (para detalle admin). */
	Club findClubById(Long idClub);

	List<Pago> listarIncidenciasPagosRecientes(int max);
}
