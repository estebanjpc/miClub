package com.app.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.entity.Categoria;
import com.app.entity.CategoriaValorVigencia;
import com.app.repository.ICategoriaRepository;
import com.app.repository.ICategoriaValorVigenciaRepository;

@Service
public class CategoriaCuotaVigenciaServiceImpl implements ICategoriaCuotaVigenciaService {

	private static final Logger log = LoggerFactory.getLogger(CategoriaCuotaVigenciaServiceImpl.class);

	private final ICategoriaValorVigenciaRepository vigenciaRepository;
	private final ICategoriaRepository categoriaRepository;

	public CategoriaCuotaVigenciaServiceImpl(ICategoriaValorVigenciaRepository vigenciaRepository,
			ICategoriaRepository categoriaRepository) {
		this.vigenciaRepository = vigenciaRepository;
		this.categoriaRepository = categoriaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer obtenerValorCuota(Long categoriaId, int anio, int mes) {
		List<CategoriaValorVigencia> aplicable = vigenciaRepository.findVigenciaAplicable(categoriaId, anio, mes,
				PageRequest.of(0, 1));
		if (!aplicable.isEmpty()) {
			return aplicable.get(0).getValorCuota();
		}

		Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);
		if (categoria == null) {
			return null;
		}

		log.warn("Sin vigencia para categoriaId={} periodo={}/{}. Se usa valor actual de categoría como fallback.", categoriaId,
				mes, anio);
		return categoria.getValorCuota();
	}

	@Override
	@Transactional
	public CategoriaValorVigencia registrarVigencia(Categoria categoria, int anio, int mes, int valorCuota) {
		if (categoria == null || categoria.getId() == null) {
			throw new IllegalArgumentException("Categoría inválida.");
		}
		if (valorCuota <= 0) {
			throw new IllegalArgumentException("El valor de cuota debe ser mayor a 0.");
		}
		if (mes < 1 || mes > 12) {
			throw new IllegalArgumentException("Mes inválido.");
		}
		if (anio < 2000 || anio > 2100) {
			throw new IllegalArgumentException("Año inválido.");
		}
		if (vigenciaRepository.existsByCategoria_IdAndAnioAndMes(categoria.getId(), anio, mes)) {
			throw new IllegalArgumentException("Ya existe una vigencia para ese mes y año.");
		}

		CategoriaValorVigencia v = new CategoriaValorVigencia();
		v.setCategoria(categoria);
		v.setAnio(anio);
		v.setMes(mes);
		v.setValorCuota(valorCuota);
		CategoriaValorVigencia saved = vigenciaRepository.save(v);

		// Se mantiene sincronizado como valor visible/actual en la categoría.
		categoria.setValorCuota(valorCuota);
		categoriaRepository.save(categoria);

		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public List<CategoriaValorVigencia> listarVigencias(Long categoriaId) {
		return vigenciaRepository.findByCategoria_IdOrderByAnioDescMesDesc(categoriaId);
	}
}
