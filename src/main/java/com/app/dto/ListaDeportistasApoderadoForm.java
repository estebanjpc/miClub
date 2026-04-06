package com.app.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.app.entity.Deportista;

public class ListaDeportistasApoderadoForm {

	private List<DeportistaApoderadoForm> filas = new ArrayList<>();

	public static ListaDeportistasApoderadoForm desdeEntidades(List<Deportista> deportistas) {
		ListaDeportistasApoderadoForm w = new ListaDeportistasApoderadoForm();
		if (deportistas == null) {
			return w;
		}
		w.setFilas(deportistas.stream().map(DeportistaApoderadoForm::desde).collect(Collectors.toList()));
		return w;
	}

	public List<DeportistaApoderadoForm> getFilas() {
		return filas;
	}

	public void setFilas(List<DeportistaApoderadoForm> filas) {
		this.filas = filas != null ? filas : new ArrayList<>();
	}
}
