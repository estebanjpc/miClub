package com.app.enums;

/**
 * Tipo de cobro asociado al registro de pago.
 * {@link #MENSUALIDAD} corresponde a cuotas mensuales del deportista.
 * {@link #INSCRIPCION} valor legado en BD para registros históricos de inscripción/matrícula (flujo UI retirado).
 */
public enum ConceptoPago {
	MENSUALIDAD,
	INSCRIPCION
}
