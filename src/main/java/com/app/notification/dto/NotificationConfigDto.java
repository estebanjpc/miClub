package com.app.notification.dto;

import com.app.notification.domain.NotificationType;

public class NotificationConfigDto {

	private NotificationType type;
	private boolean enabled;
	private int daysOffset;

	public NotificationConfigDto() {
	}

	public NotificationConfigDto(NotificationType type, boolean enabled, int daysOffset) {
		this.type = type;
		this.enabled = enabled;
		this.daysOffset = daysOffset;
	}

	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getDaysOffset() {
		return daysOffset;
	}

	public void setDaysOffset(int daysOffset) {
		this.daysOffset = daysOffset;
	}
}
