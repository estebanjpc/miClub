package com.app.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre del titular es obligatorio")
	private String nombreTitular;

	@NotBlank(message = "El RUT es obligatorio")
	private String rut;
	
//	@Email(message = "Debe ingresar un correo electrónico válido")
	@NotBlank(message = "El email es obligatorio")
	private String email;

	@NotNull(message = "Debe seleccionar un banco")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_banco")
	private Banco banco;

	@NotBlank(message = "Debe seleccionar el tipo de cuenta")
	private String tipoCuenta;

	@NotBlank(message = "Debe ingresar el número de cuenta")
	private String numeroCuenta;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_club")
	private Club club;

	private static final long serialVersionUID = 1L;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombreTitular() {
		return nombreTitular;
	}

	public void setNombreTitular(String nombreTitular) {
		this.nombreTitular = nombreTitular;
	}

	public String getRut() {
		return rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTipoCuenta() {
		return tipoCuenta;
	}

	public void setTipoCuenta(String tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

}
