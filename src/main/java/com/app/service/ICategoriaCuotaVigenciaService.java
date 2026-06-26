package com.app.service;

import java.util.List;

import com.app.entity.Categoria;
import com.app.entity.CategoriaValorVigencia;

public interface ICategoriaCuotaVigenciaService {

	Integer obtenerValorCuota(Long categoriaId, int anio, int mes);

	CategoriaValorVigencia registrarVigencia(Categoria categoria, int anio, int mes, int valorCuota);

	List<CategoriaValorVigencia> listarVigencias(Long categoriaId);
}
