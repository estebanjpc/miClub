package com.app.notification.service;

import java.time.LocalDate;
import java.time.YearMonth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.app.entity.Club;
import com.app.service.IClubService;

@Component
@ConditionalOnProperty(name = "notifications.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledCuotaReminderService {

	private static final Logger log = LoggerFactory.getLogger(ScheduledCuotaReminderService.class);

	private final IClubService clubService;
	private final CuotaReminderClubProcessor cuotaReminderClubProcessor;

	public ScheduledCuotaReminderService(IClubService clubService, CuotaReminderClubProcessor cuotaReminderClubProcessor) {
		this.clubService = clubService;
		this.cuotaReminderClubProcessor = cuotaReminderClubProcessor;
	}

	@Scheduled(cron = "${notifications.scheduler.cron:0 0 8 * * ?}")
	public void ejecutarDiario() {
		LocalDate hoy = LocalDate.now();
		YearMonth periodo = YearMonth.from(hoy);
		int mes = periodo.getMonthValue();
		int anio = periodo.getYear();

		for (Club club : clubService.findAll()) {
			if (club == null || club.getId() == null) {
				continue;
			}
			try {
				cuotaReminderClubProcessor.procesarClub(club, hoy, mes, anio);
			} catch (Exception e) {
				log.error("Error en recordatorios automáticos de cuota. clubId={}", club.getId(), e);
			}
		}
	}
}
