package com.app.notification.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.Usuario;
import com.app.notification.domain.NotificationSendLog;
import com.app.notification.domain.NotificationType;
import com.app.notification.repository.NotificationSendLogRepository;
import com.app.notification.support.CuotaDueDateCalculator;
import com.app.repository.IDeportistaRepository;
import com.app.repository.IPagoRepository;
import com.app.service.INoPagoConfigService;

@Service
public class CuotaReminderClubProcessor {

	private static final Logger log = LoggerFactory.getLogger(CuotaReminderClubProcessor.class);

	private final IDeportistaRepository deportistaRepository;
	private final IPagoRepository pagoRepository;
	private final NotificationConfigService notificationConfigService;
	private final NotificationSendLogRepository sendLogRepository;
	private final NotificationMailSender notificationMailSender;
	private final INoPagoConfigService noPagoConfigService;

	public CuotaReminderClubProcessor(IDeportistaRepository deportistaRepository, IPagoRepository pagoRepository,
			NotificationConfigService notificationConfigService, NotificationSendLogRepository sendLogRepository,
			NotificationMailSender notificationMailSender, INoPagoConfigService noPagoConfigService) {
		this.deportistaRepository = deportistaRepository;
		this.pagoRepository = pagoRepository;
		this.notificationConfigService = notificationConfigService;
		this.sendLogRepository = sendLogRepository;
		this.notificationMailSender = notificationMailSender;
		this.noPagoConfigService = noPagoConfigService;
	}

	@Transactional
	public void procesarClub(Club club, LocalDate hoy, int mes, int anio) {
		Long clubId = club.getId();
		notificationConfigService.ensureDefaultsForClub(clubId);

		boolean beforeOn = notificationConfigService.isEnabled(clubId, NotificationType.BEFORE_DUE);
		boolean afterOn = notificationConfigService.isEnabled(clubId, NotificationType.AFTER_DUE);
		if (!beforeOn && !afterOn) {
			return;
		}

		int offsetBefore = notificationConfigService.getDaysOffset(clubId, NotificationType.BEFORE_DUE);
		int offsetAfter = notificationConfigService.getDaysOffset(clubId, NotificationType.AFTER_DUE);

		LocalDate due = CuotaDueDateCalculator.dueDateForMonth(club, mes, anio);

		for (Deportista d : deportistaRepository.findAllByClubWithUsuarioAndCategoria(clubId)) {
			if (d.getFechaIngreso() != null && YearMonth.from(d.getFechaIngreso()).isAfter(YearMonth.of(anio, mes))) {
				continue;
			}
			if (noPagoConfigService.aplicaNoPago(clubId, d, mes, anio)) {
				continue;
			}
			if (pagoRepository.existsPagoBloqueante(d.getId(), mes, anio)) {
				continue;
			}
			Usuario u = d.getUsuario();
			if (u == null || u.getEmail() == null || u.getEmail().isBlank()) {
				continue;
			}

			String nombreApoderado = (u.getNombre() != null ? u.getNombre() : "") + " "
					+ (u.getApellido() != null ? u.getApellido() : "");

			if (beforeOn && hoy.equals(due.minusDays(offsetBefore))) {
				enviarSiNoDuplicado(club, d, mes, anio, NotificationType.BEFORE_DUE, u.getEmail().trim(),
						nombreApoderado.trim(), true);
			}
			if (afterOn && hoy.equals(due.plusDays(offsetAfter))) {
				enviarSiNoDuplicado(club, d, mes, anio, NotificationType.AFTER_DUE, u.getEmail().trim(),
						nombreApoderado.trim(), false);
			}
		}
	}

	private void enviarSiNoDuplicado(Club club, Deportista d, int mes, int anio, NotificationType type, String email,
			String nombreApoderado, boolean beforeDue) {
		Long clubId = club.getId();
		if (sendLogRepository.existsByClub_IdAndDeportista_IdAndNotificationTypeAndMesAndAnio(clubId, d.getId(), type,
				mes, anio)) {
			return;
		}
		boolean ok = notificationMailSender.sendCuotaReminder(email, nombreApoderado, club, d, mes, anio, beforeDue);
		if (!ok) {
			return;
		}

		NotificationSendLog logEntry = new NotificationSendLog();
		logEntry.setClub(club);
		logEntry.setDeportista(d);
		logEntry.setNotificationType(type);
		logEntry.setMes(mes);
		logEntry.setAnio(anio);
		sendLogRepository.save(logEntry);

		log.info("Recordatorio cuota enviado. type={} clubId={} deportistaId={} periodo={}-{}", type, clubId, d.getId(),
				mes, anio);
	}
}
