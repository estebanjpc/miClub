package com.app.dto;

import java.time.LocalDateTime;

public class MorosidadClubDTO {

	private Long idDeportista;
	private String nombreCompleto;
	private int cuotasAdeudadas;
	private int montoAdeudado;
	private LocalDateTime ultimaFechaPago;

	private Long idCategoria;
	private String nombreCategoria;

	public Long getIdDeportista() {
		return idDeportista;
	}

	public void setIdDeportista(Long idDeportista) {
		this.idDeportista = idDeportista;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public int getCuotasAdeudadas() {
		return cuotasAdeudadas;
	}

	public void setCuotasAdeudadas(int cuotasAdeudadas) {
		this.cuotasAdeudadas = cuotasAdeudadas;
	}

	public int getMontoAdeudado() {
		return montoAdeudado;
	}

	public void setMontoAdeudado(int montoAdeudado) {
		this.montoAdeudado = montoAdeudado;
	}

	public LocalDateTime getUltimaFechaPago() {
		return ultimaFechaPago;
	}

	public void setUltimaFechaPago(LocalDateTime ultimaFechaPago) {
		this.ultimaFechaPago = ultimaFechaPago;
	}

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
}

