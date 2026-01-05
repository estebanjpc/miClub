package com.app.dto;

public class DashboardPagoDTO {

	private Long totalDeportistas;
	private Long alDia;
	private Long morosos;
	private Integer totalRecaudadoMes;

	public Long getTotalDeportistas() {
		return totalDeportistas;
	}

	public void setTotalDeportistas(Long totalDeportistas) {
		this.totalDeportistas = totalDeportistas;
	}

	public Long getAlDia() {
		return alDia;
	}

	public void setAlDia(Long alDia) {
		this.alDia = alDia;
	}

	public Long getMorosos() {
		return morosos;
	}

	public void setMorosos(Long morosos) {
		this.morosos = morosos;
	}

	public Integer getTotalRecaudadoMes() {
		return totalRecaudadoMes;
	}

	public void setTotalRecaudadoMes(Integer totalRecaudadoMes) {
		this.totalRecaudadoMes = totalRecaudadoMes;
	}

}
