package com.app.dto;

/** Datos del comprobante de transferencia para descarga segura. */
public class ComprobanteTransferenciaDTO {

	private final byte[] data;
	private final String contentType;
	private final String nombreArchivo;

	public ComprobanteTransferenciaDTO(byte[] data, String contentType, String nombreArchivo) {
		this.data = data;
		this.contentType = contentType;
		this.nombreArchivo = nombreArchivo;
	}

	public byte[] getData() {
		return data;
	}

	public String getContentType() {
		return contentType;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

}
