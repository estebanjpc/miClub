package com.app.dto;

import java.time.LocalDateTime;

import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

public class EstadoPagoDeportistaDTO {

	private Long idDeportista;
	private String nombreCompleto;
	private Integer mes;
	private Integer anio;

	private EstadoPago estado;
	private MedioPago medioPago;

	private Long idPago; // null si no existe
	private Long idOrdenPago;
	private LocalDateTime fechaPago;



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

	public EstadoPago getEstado() {
		return estado;
	}

	public void setEstado(EstadoPago estado) {
		this.estado = estado;
	}

	public MedioPago getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(MedioPago medioPago) {
		this.medioPago = medioPago;
	}

	public Long getIdPago() {
		return idPago;
	}

	public void setIdPago(Long idPago) {
		this.idPago = idPago;
	}

	public Long getIdOrdenPago() {
		return idOrdenPago;
	}

	public void setIdOrdenPago(Long idOrdenPago) {
		this.idOrdenPago = idOrdenPago;
	}

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
	
	public String getColorEstado() {
	    return switch (estado) {
	        case PAGADO -> "bg-success";
	        case MOROSO -> "bg-danger";
	        case PENDIENTE -> "bg-warning";
	        default -> "bg-secondary";
	    };
	}

}
