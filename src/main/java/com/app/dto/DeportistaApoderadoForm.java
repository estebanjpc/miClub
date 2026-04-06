package com.app.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.app.entity.Deportista;

/**
 * Campos del deportista que el apoderado puede modificar en su perfil.
 * Categoría, estado y fecha de ingreso los define solo el club.
 */
public class DeportistaApoderadoForm {

	private Long id;
	private String nombre;
	private String apellido;
	private String rut;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fechaNacimiento;

	private String sexo;

	public static DeportistaApoderadoForm desde(Deportista d) {
		DeportistaApoderadoForm f = new DeportistaApoderadoForm();
		f.setId(d.getId());
		f.setNombre(d.getNombre());
		f.setApellido(d.getApellido());
		f.setRut(d.getRut());
		f.setFechaNacimiento(d.getFechaNacimiento());
		f.setSexo(d.getSexo());
		return f;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getRut() {
		return rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}
}
