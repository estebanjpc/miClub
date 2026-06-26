package com.app.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "asistencia_clase", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "club_id", "deportista_id", "fecha_clase" }) })
public class AsistenciaClase implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "club_id")
	private Club club;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "deportista_id")
	private Deportista deportista;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "entrenador_id")
	private Usuario entrenador;

	@Column(name = "fecha_clase")
	private LocalDate fechaClase;

	private boolean presente;

	private String observacion;

	@Column(name = "fecha_registro")
	private LocalDateTime fechaRegistro;

	@PrePersist
	public void prePersist() {
		if (fechaRegistro == null) {
			fechaRegistro = LocalDateTime.now();
		}
	}

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

	public Usuario getEntrenador() {
		return entrenador;
	}

	public void setEntrenador(Usuario entrenador) {
		this.entrenador = entrenador;
	}

	public LocalDate getFechaClase() {
		return fechaClase;
	}

	public void setFechaClase(LocalDate fechaClase) {
		this.fechaClase = fechaClase;
	}

	public boolean isPresente() {
		return presente;
	}

	public void setPresente(boolean presente) {
		this.presente = presente;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public LocalDateTime getFechaRegistro() {
		return fechaRegistro;
	}

	public void setFechaRegistro(LocalDateTime fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}
}
