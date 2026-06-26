package com.app.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.notification.domain.NotificationSendLog;
import com.app.notification.domain.NotificationType;

public interface NotificationSendLogRepository extends JpaRepository<NotificationSendLog, Long> {

	boolean existsByClub_IdAndDeportista_IdAndNotificationTypeAndMesAndAnio(Long clubId, Long deportistaId,
			NotificationType type, int mes, int anio);
}
