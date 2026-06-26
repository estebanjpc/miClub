package com.app.dto;

public class AsistenciaEstadisticaDTO {

	private Long deportistaId;
	private String deportistaNombre;
	private String categoria;
	private int clasesTotales;
	private int asistencias;
	private int ausencias;
	private double porcentajeAsistencia;

	public Long getDeportistaId() {
		return deportistaId;
	}

	public void setDeportistaId(Long deportistaId) {
		this.deportistaId = deportistaId;
	}

	public String getDeportistaNombre() {
		return deportistaNombre;
	}

	public void setDeportistaNombre(String deportistaNombre) {
		this.deportistaNombre = deportistaNombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public int getClasesTotales() {
		return clasesTotales;
	}

	public void setClasesTotales(int clasesTotales) {
		this.clasesTotales = clasesTotales;
	}

	public int getAsistencias() {
		return asistencias;
	}

	public void setAsistencias(int asistencias) {
		this.asistencias = asistencias;
	}

	public int getAusencias() {
		return ausencias;
	}

	public void setAusencias(int ausencias) {
		this.ausencias = ausencias;
	}

	public double getPorcentajeAsistencia() {
		return porcentajeAsistencia;
	}

	public void setPorcentajeAsistencia(double porcentajeAsistencia) {
		this.porcentajeAsistencia = porcentajeAsistencia;
	}
}
