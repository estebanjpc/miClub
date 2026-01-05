package com.app.dto;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class MesPagoDTO {

    private Long deportistaId;
    private String deportistaNombre;

    private Integer mes;
    private Integer anio;

    private Integer valorCuota;

    public String getNombreMes() {
        String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        if (nombre == null || nombre.isEmpty()) return nombre;
        return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
    }


    public MesPagoDTO(Long deportistaId, String deportistaNombre, Integer mes, Integer anio, Integer valorCuota) {
        this.deportistaId = deportistaId;
        this.deportistaNombre = deportistaNombre;
        this.mes = mes;
        this.anio = anio;
        this.valorCuota = valorCuota;
    }

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

	public Integer getValorCuota() {
		return valorCuota;
	}

	public void setValorCuota(Integer valorCuota) {
		this.valorCuota = valorCuota;
	}


    
}

