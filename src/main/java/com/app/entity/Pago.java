package com.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import com.app.enums.EstadoPago;
import com.app.enums.MedioPago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

@Entity
public class Pago implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	private Club club;

	@ManyToOne(fetch = FetchType.LAZY)
	private Deportista deportista;

	@Column(name = "fecha")
	private LocalDateTime fecha;

	private Integer mes;
	private Integer anio;

	@Enumerated(EnumType.STRING)
	private EstadoPago estado;

	@Enumerated(EnumType.STRING)
	private MedioPago medioPago;

	private String observacion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "orden_pago_id")
	private OrdenPago ordenPago;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public Deportista getDeportista() {
		return deportista;
	}

	public void setDeportista(Deportista deportista) {
		this.deportista = deportista;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
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

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public OrdenPago getOrdenPago() {
		return ordenPago;
	}

	public void setOrdenPago(OrdenPago ordenPago) {
		this.ordenPago = ordenPago;
	}

	@Transient
	public String getColorEstado() {
		return switch (estado) {
		case PAGADO -> "bg-success"; // Verde
		case PENDIENTE -> "bg-warning"; // Amarillo
		case PENDIENTE_KHIPU -> "bg-warning"; // Amarillo
		case RECHAZADO -> "bg-danger"; // Rojo
		case MOROSO -> "bg-danger"; // Rojo
		};
	}

	public String getNombreMes() {
		String nombre = Month.of(mes).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
		if (nombre == null || nombre.isEmpty())
			return nombre;
		return Character.toUpperCase(nombre.charAt(0)) + nombre.substring(1);
	}

}
