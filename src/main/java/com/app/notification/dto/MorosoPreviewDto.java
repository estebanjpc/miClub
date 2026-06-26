package com.app.notification.dto;

public class MorosoPreviewDto {

	private Long id;
	private String nombreCompleto;
	private String email;
	private String categoria;

	public MorosoPreviewDto() {
	}

	public MorosoPreviewDto(Long id, String nombreCompleto, String email, String categoria) {
		this.id = id;
		this.nombreCompleto = nombreCompleto;
		this.email = email;
		this.categoria = categoria;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombreCompleto() {
		return nombreCompleto;
	}

	public void setNombreCompleto(String nombreCompleto) {
		this.nombreCompleto = nombreCompleto;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}
