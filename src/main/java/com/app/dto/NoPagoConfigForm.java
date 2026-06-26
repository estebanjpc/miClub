package com.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NoPagoConfigForm {

	public enum Scope {
		CLUB, CATEGORIA, DEPORTISTA
	}

	@NotNull
	@Min(1)
	@Max(12)
	private Integer mes;

	@NotNull
	@Min(2000)
	@Max(2100)
	private Integer anio;

	@NotNull
	private Scope scope;

	private Long categoriaId;
	private Long deportistaId;

	@Size(max = 255)
	private String observacion;

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public Long getDeportistaId() {
		return deportistaId;
	}

	public void setDeportistaId(Long deportistaId) {
		this.deportistaId = deportistaId;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
}
