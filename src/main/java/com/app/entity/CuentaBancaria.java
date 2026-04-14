package com.app.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String nombreTitular;

	private String rut;

	private String email;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_banco", nullable = true)
	private Banco banco;

	private String tipoCuenta;

	private String numeroCuenta;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_club")
	private Club club;

	/** URL API Khipu (opcional; por defecto la de documentación). */
	@Column(length = 500)
	private String khipuApiUrl;

	/** API key de Khipu para crear cobros (por club). */
	@Column(length = 256)
	private String khipuApiKey;

	/** Secreto del comercio para validar webhook HMAC (por club). */
	@Column(length = 256)
	private String khipuMerchantSecret;

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

	public String getKhipuApiUrl() {
		return khipuApiUrl;
	}

	public void setKhipuApiUrl(String khipuApiUrl) {
		this.khipuApiUrl = khipuApiUrl;
	}

	public String getKhipuApiKey() {
		return khipuApiKey;
	}

	public void setKhipuApiKey(String khipuApiKey) {
		this.khipuApiKey = khipuApiKey;
	}

	public String getKhipuMerchantSecret() {
		return khipuMerchantSecret;
	}

	public void setKhipuMerchantSecret(String khipuMerchantSecret) {
		this.khipuMerchantSecret = khipuMerchantSecret;
	}

}
