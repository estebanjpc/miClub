package com.app.dto;

import java.time.LocalDateTime;

public class ConciliacionKhipuFilaDTO {

	private Long idOrden;
	private LocalDateTime fechaCreacion;
	private Integer montoTotal;
	private String khipuPaymentId;
	private String estadoOrden;
	private int cantidadPagos;
	/** Suma valor cuota de ítems en estado PAGADO */
	private long montoItemsPagados;
	private int itemsPagados;
	private int itemsPendientesOtro;

	public Long getIdOrden() {
		return idOrden;
	}

	public void setIdOrden(Long idOrden) {
		this.idOrden = idOrden;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Integer getMontoTotal() {
		return montoTotal;
	}

	public void setMontoTotal(Integer montoTotal) {
		this.montoTotal = montoTotal;
	}

	public String getKhipuPaymentId() {
		return khipuPaymentId;
	}

	public void setKhipuPaymentId(String khipuPaymentId) {
		this.khipuPaymentId = khipuPaymentId;
	}

	public String getEstadoOrden() {
		return estadoOrden;
	}

	public void setEstadoOrden(String estadoOrden) {
		this.estadoOrden = estadoOrden;
	}

	public int getCantidadPagos() {
		return cantidadPagos;
	}

	public void setCantidadPagos(int cantidadPagos) {
		this.cantidadPagos = cantidadPagos;
	}

	public long getMontoItemsPagados() {
		return montoItemsPagados;
	}

	public void setMontoItemsPagados(long montoItemsPagados) {
		this.montoItemsPagados = montoItemsPagados;
	}

	public int getItemsPagados() {
		return itemsPagados;
	}

	public void setItemsPagados(int itemsPagados) {
		this.itemsPagados = itemsPagados;
	}

	public int getItemsPendientesOtro() {
		return itemsPendientesOtro;
	}

	public void setItemsPendientesOtro(int itemsPendientesOtro) {
		this.itemsPendientesOtro = itemsPendientesOtro;
	}
}
