package com.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.app.dto.OpcionesPagoApoderadoDTO;
import com.app.entity.CuentaBancaria;

/**
 * Determina qué medios de pago puede usar el apoderado según datos configurados por el club.
 */
@Service
public class ClubMediosPagoService {

	private final ICuentaBancariaService cuentaBancariaService;

	@Value("${khipu.api.key:}")
	private String khipuApiKeyGlobal;

	public ClubMediosPagoService(ICuentaBancariaService cuentaBancariaService) {
		this.cuentaBancariaService = cuentaBancariaService;
	}

	public OpcionesPagoApoderadoDTO opcionesParaApoderado(Long idClub) {
		CuentaBancaria cb = cuentaBancariaService.findByClubId(idClub);

		boolean transferencia = cuentaBancariaCompletaParaTransferencias(cb);
		// Solo mostrar Khipu si el club guardó API key en su ficha (no depender del fallback global en la UI).
		boolean khipu = cb != null && StringUtils.hasText(cb.getKhipuApiKey());
		boolean efectivo = true;
		boolean webpay = false;

		return new OpcionesPagoApoderadoDTO(efectivo, transferencia, khipu, webpay);
	}

	/** Coincide con la lógica de cobro (BD o API key global). */
	public boolean puedeIniciarCobroKhipu(Long idClub) {
		return khipuConfiguradoParaCobros(cuentaBancariaService.findByClubId(idClub));
	}

	/** El club puede aprobar transferencias con comprobante si hay datos bancarios mínimos. */
	public boolean cuentaBancariaCompletaParaTransferencias(CuentaBancaria cb) {
		if (cb == null) {
			return false;
		}
		return cb.getBanco() != null && StringUtils.hasText(cb.getNumeroCuenta())
				&& StringUtils.hasText(cb.getNombreTitular()) && StringUtils.hasText(cb.getRut())
				&& StringUtils.hasText(cb.getEmail()) && StringUtils.hasText(cb.getTipoCuenta());
	}

	/**
	 * Puede generarse orden Khipu (misma regla que {@link com.app.service.KhipuServiceImpl}).
	 */
	public boolean khipuConfiguradoParaCobros(CuentaBancaria cb) {
		if (cb != null && StringUtils.hasText(cb.getKhipuApiKey())) {
			return true;
		}
		return StringUtils.hasText(khipuApiKeyGlobal != null ? khipuApiKeyGlobal.trim() : "");
	}

	public boolean puedeUsarTransferencia(Long idClub) {
		return cuentaBancariaCompletaParaTransferencias(cuentaBancariaService.findByClubId(idClub));
	}

	/** Coincide con {@link #opcionesParaApoderado}: solo Khipu si el club guardó API key (no el fallback global en UI). */
	public boolean puedeApoderadoUsarKhipu(Long idClub) {
		CuentaBancaria cb = cuentaBancariaService.findByClubId(idClub);
		return cb != null && StringUtils.hasText(cb.getKhipuApiKey());
	}
}
