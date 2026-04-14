package com.app.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.app.dto.EmailCampanaForm;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.EmailEnvio;
import com.app.entity.Usuario;
import com.app.repository.IEmailEnvioRepository;
import com.app.repository.IPagoRepository;

@Service
public class EmailCampanaServiceImpl implements IEmailCampanaService {

	@Autowired
	private IDeportistaService deportistaService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private IPagoRepository pagoRepository;

	@Autowired
	private IEmailEnvioRepository emailEnvioRepository;

	@Autowired
	private AsyncEmailService asyncEmailService;

	@Override
	@Transactional
	public int enviarCampana(Long clubId, EmailCampanaForm form) {
		Club club = clubService.findById(clubId);
		if (club == null) {
			return 0;
		}

		List<Deportista> base = deportistaService.listarTodosPorClub(clubId);
		List<Deportista> filtrados = aplicarFiltros(base, form);
		Set<String> correos = extraerCorreosUnicos(filtrados);

		for (Deportista d : filtrados) {
			Usuario u = d.getUsuario();
			if (u == null || !StringUtils.hasText(u.getEmail())) {
				continue;
			}
			asyncEmailService.enviarNotificacionClub(u.getEmail(), form.getAsunto(), form.getMensaje(), club.getNombre(),
					nombreCompletoUsuario(u), clubId);
		}

		EmailEnvio envio = new EmailEnvio();
		envio.setClub(club);
		envio.setTipo(form.getTipoEnvio().name());
		envio.setAsunto(form.getAsunto());
		envio.setMensaje(form.getMensaje());
		envio.setFiltrosAplicados(describirFiltros(form));
		envio.setTotalDestinatarios(correos.size());
		emailEnvioRepository.save(envio);

		return correos.size();
	}

	@Override
	@Transactional(readOnly = true)
	public List<EmailEnvio> historialPorClub(Long clubId) {
		return emailEnvioRepository.findTop50ByClubIdOrderByFechaEnvioDesc(clubId);
	}

	private List<Deportista> aplicarFiltros(List<Deportista> base, EmailCampanaForm form) {
		List<Deportista> salida = new ArrayList<>();
		for (Deportista d : base) {
			if (!cumpleCategoria(d, form.getCategoriaId())) {
				continue;
			}
			if (!cumpleEstado(d, form.getEstadoDeportista())) {
				continue;
			}
			if (!cumpleEdad(d, form.getEdadMin(), form.getEdadMax())) {
				continue;
			}
			if (requiereMorosidad(form) && !esMoroso(d, form.getMesPeriodo(), form.getAnioPeriodo())) {
				continue;
			}
			salida.add(d);
		}
		return salida;
	}

	private boolean cumpleCategoria(Deportista d, Long categoriaId) {
		if (categoriaId == null) {
			return true;
		}
		return d.getCategoria() != null && Objects.equals(d.getCategoria().getId(), categoriaId);
	}

	private boolean cumpleEstado(Deportista d, String estado) {
		if (!StringUtils.hasText(estado)) {
			return true;
		}
		return Objects.equals(estado, d.getEstado());
	}

	private boolean cumpleEdad(Deportista d, Integer min, Integer max) {
		if (d.getFechaNacimiento() == null) {
			return false;
		}
		int edad = Period.between(d.getFechaNacimiento(), LocalDate.now()).getYears();
		if (min != null && edad < min) {
			return false;
		}
		if (max != null && edad > max) {
			return false;
		}
		return true;
	}

	private boolean requiereMorosidad(EmailCampanaForm form) {
		return form.isSoloMorosos() || form.getTipoEnvio() == EmailCampanaForm.TipoEnvio.MOROSIDAD;
	}

	private boolean esMoroso(Deportista d, Integer mes, Integer anio) {
		if (d.getId() == null || mes == null || anio == null) {
			return false;
		}
		return !pagoRepository.existsPagoBloqueante(d.getId(), mes, anio);
	}

	private Set<String> extraerCorreosUnicos(List<Deportista> deportistas) {
		Set<String> set = new LinkedHashSet<>();
		for (Deportista d : deportistas) {
			if (d.getUsuario() == null || !StringUtils.hasText(d.getUsuario().getEmail())) {
				continue;
			}
			set.add(d.getUsuario().getEmail().trim().toLowerCase());
		}
		return set;
	}

	private String nombreCompletoUsuario(Usuario u) {
		String nombre = u.getNombre() != null ? u.getNombre().trim() : "";
		String apellido = u.getApellido() != null ? u.getApellido().trim() : "";
		return (nombre + " " + apellido).trim();
	}

	private String describirFiltros(EmailCampanaForm form) {
		List<String> filtros = new ArrayList<>();
		if (form.getCategoriaId() != null) {
			filtros.add("categoriaId=" + form.getCategoriaId());
		}
		if (form.getEdadMin() != null) {
			filtros.add("edadMin=" + form.getEdadMin());
		}
		if (form.getEdadMax() != null) {
			filtros.add("edadMax=" + form.getEdadMax());
		}
		if (StringUtils.hasText(form.getEstadoDeportista())) {
			filtros.add("estadoDeportista=" + form.getEstadoDeportista());
		}
		if (form.isSoloMorosos()) {
			filtros.add("soloMorosos=true");
		}
		if (form.getMesPeriodo() != null && form.getAnioPeriodo() != null) {
			filtros.add("periodo=" + form.getMesPeriodo() + "/" + form.getAnioPeriodo());
		}
		return filtros.isEmpty() ? "sin filtros" : String.join(", ", filtros);
	}
}
