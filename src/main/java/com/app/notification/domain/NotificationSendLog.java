package com.app.notification.domain;

import com.app.entity.Club;
import com.app.entity.Deportista;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "notification_send_log", uniqueConstraints = {
		@UniqueConstraint(name = "uk_notification_log_dedupe", columnNames = { "club_id", "deportista_id",
				"notification_type", "mes", "anio" }) })
public class NotificationSendLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "club_id", nullable = false)
	private Club club;

	@ManyToOne(optional = false)
	@JoinColumn(name = "deportista_id", nullable = false)
	private Deportista deportista;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 40)
	private NotificationType notificationType;

	@Column(nullable = false)
	private int mes;

	@Column(nullable = false)
	private int anio;

	@Column(name = "sent_at", nullable = false)
	private java.time.Instant sentAt = java.time.Instant.now();

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

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getAnio() {
		return anio;
	}

	public void setAnio(int anio) {
		this.anio = anio;
	}

	public java.time.Instant getSentAt() {
		return sentAt;
	}

	public void setSentAt(java.time.Instant sentAt) {
		this.sentAt = sentAt;
	}
}
