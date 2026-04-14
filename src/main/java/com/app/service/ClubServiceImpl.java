package com.app.service;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.repository.IClubRepository;
import com.app.repository.IClubHistorialCambioRepository;
import com.app.entity.Categoria;
import com.app.entity.Club;
import com.app.entity.ClubHistorialCambio;
import com.app.entity.CuentaBancaria;
import com.app.entity.Usuario;

@Service
public class ClubServiceImpl implements IClubService {

	@Autowired
	private IClubRepository clubRepository;

	@Autowired
	private IClubHistorialCambioRepository clubHistorialRepository;

	@Override
	public List<Club> findAll() {
		return (List<Club>) clubRepository.findAll();
	}

	@Override
	public Club findById(Long id) {
		return clubRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void save(Club clubNuevo, Usuario usuarioLogeado) {

		// Alta: sin id no hay historial que comparar; el flujo de edición usa findById(clubBD)
		if (clubNuevo.getId() == null) {
			clubRepository.save(clubNuevo);
			return;
		}

		Club clubBD = clubRepository.findById(clubNuevo.getId()).orElseThrow(() -> new RuntimeException("Club no encontrado"));
		Hibernate.initialize(clubBD.getCategorias());

		// ===== DATOS BÁSICOS =====
		if (!clubBD.getNombre().equals(clubNuevo.getNombre())) {
			registrarCambio(clubBD, usuarioLogeado, "DATOS_CLUB","Cambió nombre de '" + clubBD.getNombre() + "' a '" + clubNuevo.getNombre() + "'");
		}

		if (!clubBD.getTipo().equals(clubNuevo.getTipo())) {
			registrarCambio(clubBD, usuarioLogeado, "DATOS_CLUB","Cambió tipo de '" + clubBD.getTipo() + "' a '" + clubNuevo.getTipo() + "'");
		}

		if (!clubBD.getEstado().equals(clubNuevo.getEstado())) {
			registrarCambio(clubBD, usuarioLogeado, "ESTADO","Cambió estado de '" + clubBD.getEstado() + "' a '" + clubNuevo.getEstado() + "'");
		}

		if (!clubBD.getDiaVencimientoCuota().equals(clubNuevo.getDiaVencimientoCuota())) {
			registrarCambio(clubBD, usuarioLogeado, "CONFIG", "Cambió día de vencimiento de "+ clubBD.getDiaVencimientoCuota() + " a " + clubNuevo.getDiaVencimientoCuota());
		}

		// ===== CUENTA BANCARIA =====
		if (clubBD.getCuentaBancaria() != null && clubNuevo.getCuentaBancaria() != null) {
			CuentaBancaria c1 = clubBD.getCuentaBancaria();
			CuentaBancaria c2 = clubNuevo.getCuentaBancaria();

			if (!c1.getNombreTitular().equals(c2.getNombreTitular())) {
				registrarCambio(clubBD, usuarioLogeado, "CUENTA","Cambió nombre titular de '" + c1.getNombreTitular() + "' a '" + c2.getNombreTitular() + "'");
			}

			if (!c1.getNumeroCuenta().equals(c2.getNumeroCuenta())) {
				registrarCambio(clubBD, usuarioLogeado, "CUENTA","Cambió número de cuenta de '" + c1.getNumeroCuenta() + "' a '" + c2.getNumeroCuenta() + "'");
			}

			if (!c1.getBanco().getId().equals(c2.getBanco().getId())) {
				registrarCambio(clubBD, usuarioLogeado, "CUENTA", "Cambió banco");
			}
		}

		// ===== CATEGORÍAS =====
		// clubNuevo viene del formulario: la colección suele ser LAZY no cargada; solo comparar si está inicializada
		if (clubNuevo.getCategorias() != null && Hibernate.isInitialized(clubNuevo.getCategorias())) {
			for (Categoria catNueva : clubNuevo.getCategorias()) {

				Categoria catBD = clubBD.getCategorias().stream().filter(c -> c.getId().equals(catNueva.getId())).findFirst()
						.orElse(null);

				if (catBD != null) {
					if (catBD.getValorCuota() != catNueva.getValorCuota()) {
						registrarCambio(clubBD, usuarioLogeado, "CUOTA",
								"Cambió cuota de categoría '" + catBD.getNombre() + "' de " + catBD.getValorCuota() + " a "
										+ catNueva.getValorCuota());
					}

					if (!catBD.getNombre().equals(catNueva.getNombre())) {
						registrarCambio(clubBD, usuarioLogeado, "CATEGORIA",
								"Cambió nombre de categoría '" + catBD.getNombre() + "' a '" + catNueva.getNombre() + "'");
					}
				}
			}
		}

		clubRepository.save(clubNuevo);
	}

	@Override
	public boolean existsByCodigo(String codigo) {
		return clubRepository.existsByCodigo(codigo);
	}

	private void registrarCambio(Club club, Usuario usuario, String tipo, String descripcion) {
		ClubHistorialCambio h = new ClubHistorialCambio();
		h.setClub(club);
		h.setUsuario(usuario);
		h.setTipoCambio(tipo);
		h.setDescripcion(descripcion);
		clubHistorialRepository.save(h);
	}

}
