package com.app.dto;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class MesPendienteDTO {

	private String nombreDeportista;
	private Integer mes;
	private Integer anio;
	private boolean pagado;

	public MesPendienteDTO(String nombreDeportista, Integer mes, Integer anio, boolean pagado) {
		this.nombreDeportista = nombreDeportista;
		this.mes = mes;
		this.anio = anio;
		this.pagado = pagado;
	}

	public String getNombreDeportista() {
		return nombreDeportista;
	}

	public void setNombreDeportista(String nombreDeportista) {
		this.nombreDeportista = nombreDeportista;
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

	public boolean isPagado() {
		return pagado;
	}

	public void setPagado(boolean pagado) {
		this.pagado = pagado;
	}

	public String getNombreMes() {
        return Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
    }
	
}
