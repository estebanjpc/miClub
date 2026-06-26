package com.app.notification.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.entity.Club;
import com.app.notification.domain.NotificationConfig;
import com.app.notification.domain.NotificationType;
import com.app.notification.repository.NotificationConfigRepository;
import com.app.repository.IClubRepository;

@Service
public class NotificationConfigService {

	private final NotificationConfigRepository configRepository;
	private final IClubRepository clubRepository;

	public NotificationConfigService(NotificationConfigRepository configRepository, IClubRepository clubRepository) {
		this.configRepository = configRepository;
		this.clubRepository = clubRepository;
	}

	@Transactional
	public List<NotificationConfig> listForClub(Long clubId) {
		ensureDefaultsForClub(clubId);
		List<NotificationConfig> list = new ArrayList<>(configRepository.findByClub_Id(clubId));
		list.sort(Comparator.comparing(c -> c.getType().name()));
		return list;
	}

	@Transactional
	public void ensureDefaultsForClub(Long clubId) {
		Club club = clubRepository.findById(clubId).orElse(null);
		if (club == null) {
			return;
		}
		upsertIfMissing(club, NotificationType.BEFORE_DUE, true, 3);
		upsertIfMissing(club, NotificationType.AFTER_DUE, true, 5);
		upsertIfMissing(club, NotificationType.PAYMENT_RECEIVED, true, 0);
	}

	private void upsertIfMissing(Club club, NotificationType type, boolean enabled, int daysOffset) {
		if (configRepository.existsByClub_IdAndType(club.getId(), type)) {
			return;
		}
		NotificationConfig cfg = new NotificationConfig();
		cfg.setClub(club);
		cfg.setType(type);
		cfg.setEnabled(enabled);
		cfg.setDaysOffset(daysOffset);
		configRepository.save(cfg);
	}

	@Transactional
	public void update(Long clubId, NotificationType type, boolean enabled, int daysOffset) {
		ensureDefaultsForClub(clubId);
		NotificationConfig cfg = configRepository.findByClub_IdAndType(clubId, type)
				.orElseThrow(() -> new IllegalArgumentException("Configuración no encontrada"));
		cfg.setEnabled(enabled);
		if (type != NotificationType.PAYMENT_RECEIVED) {
			cfg.setDaysOffset(Math.max(0, Math.min(daysOffset, 60)));
		} else {
			cfg.setDaysOffset(0);
		}
		configRepository.save(cfg);
	}

	@Transactional
	public boolean isEnabled(Long clubId, NotificationType type) {
		ensureDefaultsForClub(clubId);
		return configRepository.findByClub_IdAndType(clubId, type).map(NotificationConfig::isEnabled).orElse(false);
	}

	@Transactional
	public int getDaysOffset(Long clubId, NotificationType type) {
		ensureDefaultsForClub(clubId);
		return configRepository.findByClub_IdAndType(clubId, type).map(NotificationConfig::getDaysOffset).orElse(0);
	}
}
