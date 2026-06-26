package com.app.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.notification.domain.NotificationConfig;
import com.app.notification.domain.NotificationType;

public interface NotificationConfigRepository extends JpaRepository<NotificationConfig, Long> {

	List<NotificationConfig> findByClub_Id(Long clubId);

	Optional<NotificationConfig> findByClub_IdAndType(Long clubId, NotificationType type);

	boolean existsByClub_IdAndType(Long clubId, NotificationType type);
}
