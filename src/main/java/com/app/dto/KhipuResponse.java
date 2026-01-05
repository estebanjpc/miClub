package com.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KhipuResponse {

	private String paymentId;
	private String paymentUrl;
	private String simplifiedTransferUrl;
	private String status;
	private Integer amount;
	private String subject;

	public KhipuResponse() {
	}

	public KhipuResponse(String paymentId, String paymentUrl) {
		this.paymentId = paymentId;
		this.paymentUrl = paymentUrl;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}

	public String getSimplifiedTransferUrl() {
		return simplifiedTransferUrl;
	}

	public void setSimplifiedTransferUrl(String simplifiedTransferUrl) {
		this.simplifiedTransferUrl = simplifiedTransferUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
