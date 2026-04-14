package com.app.dto;

/**
 * Medios de pago que el apoderado puede ver según la configuración del club.
 */
public record OpcionesPagoApoderadoDTO(boolean efectivo, boolean transferencia, boolean khipu, boolean webpay) {

	public boolean algunoDisponibleAdemasDeEfectivo() {
		return transferencia || khipu || webpay;
	}
}
