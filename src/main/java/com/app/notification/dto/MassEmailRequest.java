package com.app.notification.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MassEmailRequest {

	private String subject;
	private String message;
	private List<Long> categoryIds;

	@JsonAlias({ "selectedUserIds", "selectedDeportistaIds" })
	private List<Long> selectedDeportistaIds;

	private MassEmailFilter filter = MassEmailFilter.ALL;

	private Integer month;
	private Integer year;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Long> getCategoryIds() {
		return categoryIds;
	}

	public void setCategoryIds(List<Long> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public List<Long> getSelectedDeportistaIds() {
		return selectedDeportistaIds;
	}

	public void setSelectedDeportistaIds(List<Long> selectedDeportistaIds) {
		this.selectedDeportistaIds = selectedDeportistaIds;
	}

	public MassEmailFilter getFilter() {
		return filter;
	}

	public void setFilter(MassEmailFilter filter) {
		this.filter = filter;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}
}
