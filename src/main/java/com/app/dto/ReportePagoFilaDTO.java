package com.app.dto;

public class ReportePagoFilaDTO {

	private String deportista;
	private String categoria;
	private String periodoCuota;
	private String estado;
	private String medio;
	private long monto;
	private String fechaRegistro;

	public String getDeportista() {
		return deportista;
	}

	public void setDeportista(String deportista) {
		this.deportista = deportista;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getPeriodoCuota() {
		return periodoCuota;
	}

	public void setPeriodoCuota(String periodoCuota) {
		this.periodoCuota = periodoCuota;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getMedio() {
		return medio;
	}

	public void setMedio(String medio) {
		this.medio = medio;
	}

	public long getMonto() {
		return monto;
	}

	public void setMonto(long monto) {
		this.monto = monto;
	}

	public String getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(String fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}
}
