package com.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EmailCampanaForm {

	@NotNull(message = "Debe seleccionar un tipo de envío")
	private TipoEnvio tipoEnvio;

	@NotBlank(message = "El asunto es obligatorio")
	@Size(max = 180, message = "El asunto no puede superar 180 caracteres")
	private String asunto;

	@NotBlank(message = "El mensaje es obligatorio")
	@Size(max = 4000, message = "El mensaje no puede superar 4000 caracteres")
	private String mensaje;

	private Long categoriaId;

	@Min(value = 0, message = "Edad mínima inválida")
	@Max(value = 100, message = "Edad mínima inválida")
	private Integer edadMin;

	@Min(value = 0, message = "Edad máxima inválida")
	@Max(value = 100, message = "Edad máxima inválida")
	private Integer edadMax;

	/** "1" activo, "2" desactivo */
	private String estadoDeportista;

	private boolean soloMorosos;

	@Min(value = 1, message = "Mes inválido")
	@Max(value = 12, message = "Mes inválido")
	private Integer mesPeriodo;

	@Min(value = 2000, message = "Año inválido")
	@Max(value = 2100, message = "Año inválido")
	private Integer anioPeriodo;

	public enum TipoEnvio {
		MASIVO,
		MOROSIDAD,
		EVENTO,
		BIENVENIDA,
		PERSONALIZADO
	}

	public TipoEnvio getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(TipoEnvio tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
	}

	public String getAsunto() {
		return asunto;
	}

	public void setAsunto(String asunto) {
		this.asunto = asunto;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public Integer getEdadMin() {
		return edadMin;
	}

	public void setEdadMin(Integer edadMin) {
		this.edadMin = edadMin;
	}

	public Integer getEdadMax() {
		return edadMax;
	}

	public void setEdadMax(Integer edadMax) {
		this.edadMax = edadMax;
	}

	public String getEstadoDeportista() {
		return estadoDeportista;
	}

	public void setEstadoDeportista(String estadoDeportista) {
		this.estadoDeportista = estadoDeportista;
	}

	public boolean isSoloMorosos() {
		return soloMorosos;
	}

	public void setSoloMorosos(boolean soloMorosos) {
		this.soloMorosos = soloMorosos;
	}

	public Integer getMesPeriodo() {
		return mesPeriodo;
	}

	public void setMesPeriodo(Integer mesPeriodo) {
		this.mesPeriodo = mesPeriodo;
	}

	public Integer getAnioPeriodo() {
		return anioPeriodo;
	}

	public void setAnioPeriodo(Integer anioPeriodo) {
		this.anioPeriodo = anioPeriodo;
	}
}
