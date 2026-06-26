package com.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CobroAdicionalForm {

	public enum TipoCobro {
		MATRICULA, IMPLEMENTACION, OTRO
	}

	public enum Alcance {
		DEPORTISTA, CATEGORIA, TODOS
	}

	@NotNull
	private TipoCobro tipoCobro;

	@Size(max = 255)
	private String descripcion;

	@NotNull
	@Min(1)
	private Integer monto;

	@NotNull
	@Min(1)
	@Max(12)
	private Integer mes;

	@NotNull
	@Min(2000)
	@Max(2100)
	private Integer anio;

	@NotNull
	private Alcance alcance;

	private Long deportistaId;
	private Long categoriaId;

	public TipoCobro getTipoCobro() {
		return tipoCobro;
	}

	public void setTipoCobro(TipoCobro tipoCobro) {
		this.tipoCobro = tipoCobro;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getMonto() {
		return monto;
	}

	public void setMonto(Integer monto) {
		this.monto = monto;
	}

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

	public Alcance getAlcance() {
		return alcance;
	}

	public void setAlcance(Alcance alcance) {
		this.alcance = alcance;
	}

	public Long getDeportistaId() {
		return deportistaId;
	}

	public void setDeportistaId(Long deportistaId) {
		this.deportistaId = deportistaId;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}
}
