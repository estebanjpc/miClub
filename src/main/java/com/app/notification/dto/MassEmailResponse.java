package com.app.notification.dto;

public class MassEmailResponse {

	private final int recipientsQueued;

	public MassEmailResponse(int recipientsQueued) {
		this.recipientsQueued = recipientsQueued;
	}

	public int getRecipientsQueued() {
		return recipientsQueued;
	}
}
