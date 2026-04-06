package com.app.service;

import java.util.List;

import com.app.dto.ConciliacionKhipuFilaDTO;
import com.app.dto.FinancieroDashboardDTO;
import com.app.dto.ReportePagoFilaDTO;
import com.app.enums.MedioPago;

public interface IFinancieroClubService {

	FinancieroDashboardDTO obtenerDashboard(Long idClub, Integer mesDesde, Integer anioDesde, Integer mesHasta,
			Integer anioHasta);

	List<ReportePagoFilaDTO> obtenerFilasReporte(Long idClub, int mesDesde, int anioDesde, int mesHasta, int anioHasta,
			Long idCategoria, MedioPago medio);

	List<ConciliacionKhipuFilaDTO> obtenerConciliacionKhipu(Long idClub);
}
