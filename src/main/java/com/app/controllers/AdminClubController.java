package com.app.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.app.entity.Club;
import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.service.IClubService;
import com.app.service.AsyncEmailService;
import com.app.service.IAdminPanelService;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@Secured("ROLE_ADMIN")
@SessionAttributes("usuario")
public class AdminClubController {

	private static final Logger log = LoggerFactory.getLogger(AdminClubController.class);

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private AsyncEmailService asyncEmailService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@Autowired
	private IAdminPanelService adminPanelService;

	@GetMapping({ "/listadoClub" })
	public String listadoClub(Model model) {
		model.addAttribute("titulo", "Mantenedor Club");
		model.addAttribute("listadoClub", adminPanelService.listarClubesConConteos());
		return "listadoClub";
	}

	@GetMapping({ "/crearClub" })
	public String crearClub(Map<String, Object> model) {
		Usuario usuario = new Usuario();
		Role rolClub = new Role("Club", "ROLE_CLUB");
		usuario.setRoles(new ArrayList<>(Arrays.asList(rolClub)));
		rolClub.setUsuario(usuario);
		Club club = new Club();
		club.setEstado("1");
		club.setDiaVencimientoCuota(1);
		usuario.setClub(club);
		// El formulario solo envía club.estado; @NotEmpty en usuario.estado exige valor antes de @Valid
		usuario.setEstado(club.getEstado());
		model.put("titulo", "Mantenedor Club");
		model.put("usuario", usuario);
		model.put("btn", "Crear");
		model.put("btnVolver", "/listadoClub");
		model.put("adminClubForm", Boolean.TRUE);
		return "club";
	}

	@PostMapping("/guardarClub")
	public String guardarUsuario(@Valid Usuario usuario, BindingResult result,
			@RequestParam(required = false) MultipartFile fileLogo,
			@RequestParam(required = false) String eliminarLogo,
			Map<String, Object> model, RedirectAttributes flash,HttpServletRequest request, SessionStatus status) {

		
		Usuario usuarioLogin = (Usuario) request.getSession().getAttribute("usuarioLogin");
		
		model.put("titulo", "Mantenedor Club");
		boolean esNuevoClub = usuario.getId() == null;
		model.put("btn", esNuevoClub ? "Crear" : "Actualizar");
		model.put("adminClubForm", Boolean.TRUE);
		model.put("btnVolver", "/listadoClub");

		String msje = "Editado con Éxito";
		String pass = "";
		boolean flagNuevo = false;

		if (result.hasErrors()) {
			model.put("adminClubForm", Boolean.TRUE);
			model.put("btnVolver", "/listadoClub");
			model.put("btn", esNuevoClub ? "Crear" : "Actualizar");
			return "club";
		}

		boolean emailDuplicado;
		if (usuario.getId() != null && usuario.getClub() != null && usuario.getClub().getId() != null) {
			emailDuplicado = usuarioService.existsOtroUsuarioConEmailEnClub(usuario.getEmail(),
					usuario.getClub().getId(), usuario.getId());
		} else {
			List<Usuario> mismoCorreo = usuarioService.findAllByEmail(usuario.getEmail());
			emailDuplicado = !mismoCorreo.isEmpty()
					&& (usuario.getId() == null || mismoCorreo.stream().anyMatch(u -> !u.getId().equals(usuario.getId())));
		}
		if (emailDuplicado) {
			result.rejectValue("email", "error.usuario", "Ya existe un usuario registrado con este correo electrónico");
			model.put("msjLayout", "error;Correo duplicado;Ya existe un usuario registrado con ese email.");
			model.put("adminClubForm", Boolean.TRUE);
			model.put("btnVolver", "/listadoClub");
			model.put("btn", esNuevoClub ? "Crear" : "Actualizar");
			return "club";
		}

		if ("true".equals(eliminarLogo)) {
	        usuario.getClub().setLogo(null);
	    } else if (fileLogo != null && !fileLogo.isEmpty()) {
	        try {
	        	usuario.getClub().setLogo(fileLogo.getBytes());
	        } catch (IOException e) {
	        	log.error("Error cargando logo para club. email={} nombreClub={}", usuario.getEmail(), usuario.getNombre(), e);
	        }
	    }
		
		// NUEVO usuario
		if (usuario.getId() == null) {
			flagNuevo = true;
			pass = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(pass));
			usuario.setEstado("0"); // debe cambiar clave al ingresar (no se sobrescribe con estado del club)
			msje = "Creado con Éxito";
			
			// Generar código único para el club
	        String baseSlug = Util.toSlug(usuario.getNombre());
	        String uniqueSlug = baseSlug;
	        int counter = 1;

	        while (clubService.existsByCodigo(uniqueSlug)) {
	            uniqueSlug = baseSlug + "-" + counter;
	            counter++;
	        }

	        usuario.getClub().setCodigo(uniqueSlug);
	        
		} else {
			// EXISTENTE: conservar estado original
			Usuario existente = usuarioService.findById(usuario.getId());
			if (existente != null) {
//				usuario.setEstado(existente.getEstado());
				usuario.setPassword(existente.getPassword());
				usuario.getClub().setCodigo(existente.getClub().getCodigo());
			}
		}

		usuario.setEnabled("1".equals(usuario.getClub().getEstado()));
		// Alta: mantener estado "0" hasta que el usuario cambie la clave temporal (no copiar estado del club).
		if (!flagNuevo) {
			usuario.setEstado(usuario.getClub().getEstado());
		} else {
			usuario.setEstado("0");
		}
		usuario.getClub().setNombre(usuario.getNombre());

		clubService.save(usuario.getClub(),usuarioLogin);
		asegurarRolClubYEnlace(usuario);
		usuarioService.save(usuario);
		status.setComplete();
		flash.addFlashAttribute("msjLayout", "success;" + msje + "!;Club " + msje);

		if (flagNuevo) {
			usuario.setPassAux(pass);
			asyncEmailService.creacionClub(usuario);
		}

		return "redirect:/listadoClub";
	}

	@GetMapping({ "/editarClub/{id}" })
	public String editarClub(@PathVariable Long id, Map<String, Object> model, RedirectAttributes flash) {
		Usuario usuario = usuarioService.findById(id);
		if (usuario == null) {
			model.put("msjLayout", "error;Club no existe;Clubno existe en BD");
			return "listadoClub";
		}
		usuario.setEstado(usuario.getEnabled() ? "1" : "2");
		model.put("titulo", "Mantenedor Club");
		model.put("usuario", usuario);
		model.put("btn", "Actualizar");
		model.put("btnVolver", "/listadoClub");
		model.put("adminClubForm", Boolean.TRUE);
		return "club";
	}

	/**
	 * El listado usa {@code JOIN u.roles}; cada {@link Role} debe tener {@code usuario} fijado y existir ROLE_CLUB.
	 * El POST puede dejar {@code roles} vacío respecto al GET (sesión).
	 */
	private static void asegurarRolClubYEnlace(Usuario usuario) {
		if (usuario.getRoles() == null) {
			usuario.setRoles(new ArrayList<>());
		}
		boolean tieneRolClub = usuario.getRoles().stream().anyMatch(r -> "ROLE_CLUB".equals(r.getAuthority()));
		if (!tieneRolClub) {
			Role r = new Role("Club", "ROLE_CLUB");
			r.setUsuario(usuario);
			usuario.getRoles().add(r);
		}
		for (Role r : usuario.getRoles()) {
			r.setUsuario(usuario);
		}
	}

	@ModelAttribute("estados")
	public Map<String, String> estados() {
		Map<String, String> estados = new HashMap<String, String>();
		estados.put("1", "Activo");
		estados.put("2", "Desactivo");
		return estados;
	}

	@ModelAttribute("listaTipoClub")
	public Map<String, String> listaTipoClub() {
		Map<String, String> lista = new HashMap<String, String>();
		lista.put("Baquetball", "Baquetball");
		lista.put("Futbol", "Futbol");
		lista.put("Pin-Pong", "Pin-Pong");
		lista.put("MultiDeportes", "MultiDeportes");
		return lista;
	}

}
