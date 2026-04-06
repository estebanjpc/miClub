package com.app.dto;

/**
 * Conteos por categoría para el mes consultado (estado mensual).
 */
public class ResumenCategoriaDTO {

	private Long idCategoria;
	private String nombreCategoria;
	private int morosos;
	private int pendientesPago;

	public Long getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(Long idCategoria) {
		this.idCategoria = idCategoria;
	}

	public String getNombreCategoria() {
		return nombreCategoria;
	}

	public void setNombreCategoria(String nombreCategoria) {
		this.nombreCategoria = nombreCategoria;
	}

	public int getMorosos() {
		return morosos;
	}

	public void setMorosos(int morosos) {
		this.morosos = morosos;
	}

	public int getPendientesPago() {
		return pendientesPago;
	}

	public void setPendientesPago(int pendientesPago) {
		this.pendientesPago = pendientesPago;
	}

	public int getTotalDeben() {
		return morosos + pendientesPago;
	}
}
