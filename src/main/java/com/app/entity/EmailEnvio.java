package com.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_envios")
public class EmailEnvio implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_club", nullable = false)
	private Club club;

	@Column(nullable = false, length = 80)
	private String tipo;

	@Column(nullable = false, length = 180)
	private String asunto;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String mensaje;

	@Column(name = "filtros_aplicados", length = 500)
	private String filtrosAplicados;

	@Column(name = "total_destinatarios", nullable = false)
	private Integer totalDestinatarios;

	@Column(name = "fecha_envio", nullable = false)
	private LocalDateTime fechaEnvio;

	@PrePersist
	void prePersist() {
		if (this.fechaEnvio == null) {
			this.fechaEnvio = LocalDateTime.now();
		}
		if (this.totalDestinatarios == null) {
			this.totalDestinatarios = 0;
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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getFiltrosAplicados() {
		return filtrosAplicados;
	}

	public void setFiltrosAplicados(String filtrosAplicados) {
		this.filtrosAplicados = filtrosAplicados;
	}

	public Integer getTotalDestinatarios() {
		return totalDestinatarios;
	}

	public void setTotalDestinatarios(Integer totalDestinatarios) {
		this.totalDestinatarios = totalDestinatarios;
	}

	public LocalDateTime getFechaEnvio() {
		return fechaEnvio;
	}

	public void setFechaEnvio(LocalDateTime fechaEnvio) {
		this.fechaEnvio = fechaEnvio;
	}
}
