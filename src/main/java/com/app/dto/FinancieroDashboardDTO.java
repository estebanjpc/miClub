package com.app.dto;

public class FinancieroDashboardDTO {

	private long totalRecaudado;
	private long deudaTotal;
	private long cantidadMorosos;
	private int mesDesde;
	private int anioDesde;
	private int mesHasta;
	private int anioHasta;
	private String etiquetaPeriodo;

	public long getTotalRecaudado() {
		return totalRecaudado;
	}

	public void setTotalRecaudado(long totalRecaudado) {
		this.totalRecaudado = totalRecaudado;
	}

	public long getDeudaTotal() {
		return deudaTotal;
	}

	public void setDeudaTotal(long deudaTotal) {
		this.deudaTotal = deudaTotal;
	}

	public long getCantidadMorosos() {
		return cantidadMorosos;
	}

	public void setCantidadMorosos(long cantidadMorosos) {
		this.cantidadMorosos = cantidadMorosos;
	}

	public int getMesDesde() {
		return mesDesde;
	}

	public void setMesDesde(int mesDesde) {
		this.mesDesde = mesDesde;
	}

	public int getAnioDesde() {
		return anioDesde;
	}

	public void setAnioDesde(int anioDesde) {
		this.anioDesde = anioDesde;
	}

	public int getMesHasta() {
		return mesHasta;
	}

	public void setMesHasta(int mesHasta) {
		this.mesHasta = mesHasta;
	}

	public int getAnioHasta() {
		return anioHasta;
	}

	public void setAnioHasta(int anioHasta) {
		this.anioHasta = anioHasta;
	}

	public String getEtiquetaPeriodo() {
		return etiquetaPeriodo;
	}

	public void setEtiquetaPeriodo(String etiquetaPeriodo) {
		this.etiquetaPeriodo = etiquetaPeriodo;
	}
}
