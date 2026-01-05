package com.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

import jakarta.persistence.*;

@Entity
public class OrdenPago implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long idUsuario;

	private Integer montoTotal;

	private LocalDateTime fechaCreacion;

	@Enumerated(EnumType.STRING)
	private MedioPago medioPago;

	private String khipuPaymentId;

	private String khipuUrl;
	
	@Enumerated(EnumType.STRING)
	private EstadoPago estado;

	private LocalDateTime fechaPago;

	private String khipuTransactionId;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "orden_pago_id")
	private List<Pago> pagos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Long idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getMontoTotal() {
		return montoTotal;
	}

	public void setMontoTotal(Integer montoTotal) {
		this.montoTotal = montoTotal;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public MedioPago getMedioPago() {
		return medioPago;
	}

	public void setMedioPago(MedioPago medioPago) {
		this.medioPago = medioPago;
	}

	public String getKhipuPaymentId() {
		return khipuPaymentId;
	}

	public void setKhipuPaymentId(String khipuPaymentId) {
		this.khipuPaymentId = khipuPaymentId;
	}

	public String getKhipuUrl() {
		return khipuUrl;
	}

	public void setKhipuUrl(String khipuUrl) {
		this.khipuUrl = khipuUrl;
	}

	public List<Pago> getPagos() {
		return pagos;
	}

	public void setPagos(List<Pago> pagos) {
		this.pagos = pagos;
	}
	
	
	
	
	public EstadoPago getEstado() {
		return estado;
	}

	public void setEstado(EstadoPago estado) {
		this.estado = estado;
	}

	public LocalDateTime getFechaPago() {
		return fechaPago;
	}

	public void setFechaPago(LocalDateTime fechaPago) {
		this.fechaPago = fechaPago;
	}

	public String getKhipuTransactionId() {
		return khipuTransactionId;
	}

	public void setKhipuTransactionId(String khipuTransactionId) {
		this.khipuTransactionId = khipuTransactionId;
	}

	@PrePersist
	public void prePersist() {
	    this.fechaCreacion = LocalDateTime.now();
	    if (this.estado == null) {
	        this.estado = EstadoPago.PENDIENTE_KHIPU;
	    }
	}
	
}
