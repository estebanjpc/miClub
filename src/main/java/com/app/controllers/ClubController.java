package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.TipoAltaUsuarioClub;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.service.AsyncEmailService;
import com.app.service.ClubMediosPagoService;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.IDeportistaService;
import com.app.security.AppRoles;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("usuario")
public class ClubController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@Autowired
	private AsyncEmailService asyncEmailService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private ICategoriaService categoriaService;

	@Autowired
	private IDeportistaService deportistaService;

	@Autowired
	private ClubMediosPagoService clubMediosPagoService;

	@GetMapping("/listadoUsuarios")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String listadoUsuariosRedirect() {
		return "redirect:/listadoDeportistas";
	}

	@GetMapping("/listadoDeportistas")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String listadoDeportistas(Model model, HttpServletRequest request, Principal principal,
			RedirectAttributes flash) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		if (usuarioService.refrescarUsuarioSesion(request, principal.getName()) == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		Club club = clubService.findById(idClubSession);
		if (club == null || !"1".equals(club.getEstado())) {
			return "redirect:/login?clubDeshabilitado";
		}

		List<Deportista> listadoDeportistas = deportistaService.listarTodosPorClub(idClubSession);

		var opcionesPago = clubMediosPagoService.opcionesParaApoderado(idClubSession);
		boolean soloEfectivoMediosPago = !opcionesPago.transferencia() && !opcionesPago.khipu();

		model.addAttribute("titulo", "Listado de deportistas");
		model.addAttribute("listadoDeportistas", listadoDeportistas);
		model.addAttribute("soloEfectivoMediosPago", soloEfectivoMediosPago);
		List<Usuario> personalStaff = usuarioService.findUsuarioByIdClubAndRoles(idClubSession,
				List.of(AppRoles.TESORERO, AppRoles.ENTRENADOR));
		model.addAttribute("personalStaff", personalStaff);
		return "listadoDeportistas";
	}

	@GetMapping("/crearUsuario")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String crearUsuario(
			@RequestParam(required = false, defaultValue = TipoAltaUsuarioClub.APODERADO) String tipo,
			Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash,
			Authentication authentication) {

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		boolean puedeCrearStaff = esRolClub(authentication);
		String tipoNorm = tipo != null ? tipo.trim().toUpperCase() : TipoAltaUsuarioClub.APODERADO;
		if (!TipoAltaUsuarioClub.esValido(tipoNorm)) {
			tipoNorm = TipoAltaUsuarioClub.APODERADO;
		}
		if (!puedeCrearStaff && (TipoAltaUsuarioClub.TESORERO.equals(tipoNorm)
				|| TipoAltaUsuarioClub.ENTRENADOR.equals(tipoNorm))) {
			tipoNorm = TipoAltaUsuarioClub.APODERADO;
		}

		boolean esStaffCreacion = TipoAltaUsuarioClub.TESORERO.equals(tipoNorm)
				|| TipoAltaUsuarioClub.ENTRENADOR.equals(tipoNorm);

		Usuario usuario = new Usuario();
		if (esStaffCreacion) {
			Role r = TipoAltaUsuarioClub.TESORERO.equals(tipoNorm)
					? new Role("Tesorero", AppRoles.TESORERO)
					: new Role("Entrenador", AppRoles.ENTRENADOR);
			usuario.setRoles(new ArrayList<>(List.of(r)));
			r.setUsuario(usuario);
			usuario.setDeportistas(new ArrayList<>());
		} else {
			usuario.setRoles(new ArrayList<>(List.of(new Role("Usuario", AppRoles.USER))));
			usuario.setDeportistas(new ArrayList<>());
			usuario.getDeportistas().add(new Deportista());
		}

		model.addAttribute("titulo", esStaffCreacion ? "Crear personal del club" : "Crear Usuario / Apoderado");
		model.addAttribute("usuario", usuario);
		model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
		model.addAttribute("estados", estados());
		model.addAttribute("isEditing", false);
		model.addAttribute("btn", "Crear");
		model.addAttribute("puedeCrearStaff", puedeCrearStaff);
		model.addAttribute("tipoAltaSeleccionado", tipoNorm);
		model.addAttribute("esStaffCreacion", esStaffCreacion);
		model.addAttribute("esStaff", esStaffCreacion);

		return "usuario";
	}

	@PostMapping("/guardarUsuarioClub")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String guardarUsuarioClub(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result,
			@RequestParam(required = false, defaultValue = TipoAltaUsuarioClub.APODERADO) String tipoAltaUsuario,
			Model model, SessionStatus status, RedirectAttributes flash, HttpServletRequest request, Principal principal,
			Authentication authentication) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		String tipoNorm = tipoAltaUsuario != null ? tipoAltaUsuario.trim().toUpperCase() : TipoAltaUsuarioClub.APODERADO;
		if (!TipoAltaUsuarioClub.esValido(tipoNorm)) {
			tipoNorm = TipoAltaUsuarioClub.APODERADO;
		}
		boolean puedeCrearStaff = esRolClub(authentication);
		if (!puedeCrearStaff && (TipoAltaUsuarioClub.TESORERO.equals(tipoNorm)
				|| TipoAltaUsuarioClub.ENTRENADOR.equals(tipoNorm))) {
			tipoNorm = TipoAltaUsuarioClub.APODERADO;
		}

		boolean nuevo = (usuario.getId() == null);
		Usuario persistido = null;
		if (!nuevo) {
			persistido = usuarioService.findByIdWithRolesAndDeportistas(usuario.getId());
			if (persistido == null || persistido.getClub() == null || !idClubSession.equals(persistido.getClub().getId())) {
				flash.addFlashAttribute("msjLayout", "error;Error;Usuario no encontrado.");
				return "redirect:/listadoDeportistas";
			}
		}

		boolean esStaffOperacion = nuevo
				? (TipoAltaUsuarioClub.TESORERO.equals(tipoNorm) || TipoAltaUsuarioClub.ENTRENADOR.equals(tipoNorm))
				: esUsuarioStaff(persistido);

		if (!nuevo && esUsuarioStaff(persistido) && !esRolClub(authentication)) {
			flash.addFlashAttribute("msjLayout", "error;Error;No tiene permiso para editar este usuario.");
			return "redirect:/listadoDeportistas";
		}

		if (nuevo && esStaffOperacion && !esRolClub(authentication)) {
			flash.addFlashAttribute("msjLayout", "error;Error;Solo el administrador del club puede crear este tipo de usuario.");
			return "redirect:/listadoDeportistas";
		}

		if (usuario.getDeportistas() != null) {
			usuario.setDeportistas(usuario.getDeportistas().stream()
					.filter(d -> (d.getNombre() != null && !d.getNombre().isBlank())
							|| (d.getApellido() != null && !d.getApellido().isBlank())
							|| (d.getRut() != null && !d.getRut().isBlank()) || d.getFechaNacimiento() != null
							|| (d.getSexo() != null && !d.getSexo().isBlank()) || d.getCategoria() != null)
					.collect(java.util.stream.Collectors.toList()));
		}

		if (esStaffOperacion) {
			usuario.setDeportistas(new ArrayList<>());
		}

		validarUsuario(usuario, result);
		if (!esStaffOperacion) {
			validarDeportistasApoderado(usuario, result);
		}

		if (result.hasErrors()) {
			String tituloErr;
			if (esStaffOperacion) {
				tituloErr = usuario.getId() != null ? "Actualizar personal del club" : "Crear personal del club";
			} else {
				tituloErr = usuario.getId() != null ? "Actualizar Datos Usuario" : "Crear Usuario / Apoderado";
			}
			model.addAttribute("titulo", tituloErr);
			model.addAttribute("btn", usuario.getId() != null ? "Actualizar" : "Guardar");
			model.addAttribute("isEditing", usuario.getId() != null);
			model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
			model.addAttribute("estados", estados());
			model.addAttribute("puedeCrearStaff", puedeCrearStaff);
			model.addAttribute("tipoAltaSeleccionado", tipoNorm);
			model.addAttribute("esStaffCreacion", nuevo && esStaffOperacion);
			model.addAttribute("esStaff", esStaffOperacion);
			return "usuario";
		}

		String passGenerada = null;
		String mensaje = nuevo ? "Creado" : "Editado";
		Club club = clubService.findById(idClubSession);

		if (nuevo) {
			if (esStaffOperacion) {
				Role r = TipoAltaUsuarioClub.TESORERO.equals(tipoNorm)
						? new Role("Tesorero", AppRoles.TESORERO)
						: new Role("Entrenador", AppRoles.ENTRENADOR);
				usuario.setRoles(new ArrayList<>(List.of(r)));
				r.setUsuario(usuario);
			} else {
				usuario.setRoles(new ArrayList<>(List.of(new Role("Usuario", AppRoles.USER))));
				usuario.getRoles().get(0).setUsuario(usuario);
			}

			usuario.setClub(club);
			usuario.setEnabled("1".equals(usuario.getEstado()));
			passGenerada = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(passGenerada));
			usuario.setEstado("0");
			usuario.setEnabled("1".equals(usuario.getEstado()));

			if (!esStaffOperacion && usuario.getDeportistas() != null) {
				usuario.getDeportistas().forEach(d -> {
					d.setUsuario(usuario);
					if (d.getFechaIngreso() == null) {
						d.setFechaIngreso(LocalDate.now());
					}
				});
			}

			usuarioService.save(usuario);
			status.setComplete();

			usuario.setPassAux(passGenerada);
			asyncEmailService.creacionUsuario(usuario);

			flash.addFlashAttribute("msjLayout", "success;" + mensaje + " con éxito;Usuario " + mensaje);
			return "redirect:/listadoDeportistas";
		}

		// Edición
		if (esStaffOperacion) {
			copiarDatosBasicos(usuario, persistido);
			persistido.setClub(club);
			persistido.setEnabled("1".equals(persistido.getEstado()));
			persistido.getDeportistas().clear();
			usuarioService.save(persistido);
		} else {
			copiarDatosBasicos(usuario, persistido);
			persistido.setClub(club);
			persistido.setEnabled("1".equals(persistido.getEstado()));

			persistido.getDeportistas().clear();
			if (usuario.getDeportistas() != null) {
				for (Deportista d : usuario.getDeportistas()) {
					d.setUsuario(persistido);
					if (d.getFechaIngreso() == null) {
						d.setFechaIngreso(LocalDate.now());
					}
					persistido.getDeportistas().add(d);
				}
			}
			usuarioService.save(persistido);
		}

		status.setComplete();
		flash.addFlashAttribute("msjLayout", "success;" + mensaje + " con éxito;Usuario " + mensaje);

		return "redirect:/listadoDeportistas";
	}

	@GetMapping("/editarUsuario/{id}")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public String editarUsuario(@PathVariable Long id, Model model, HttpServletRequest request, Principal principal,
			RedirectAttributes flash, Authentication authentication) {

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		Usuario usuario = usuarioService.findByIdWithRolesAndDeportistas(id);

		if (usuario == null) {
			return "redirect:/listadoDeportistas";
		}

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (usuario.getClub() == null || !idClubSession.equals(usuario.getClub().getId())) {
			return "redirect:/listadoDeportistas";
		}

		if (esUsuarioStaff(usuario) && !esRolClub(authentication)) {
			flash.addFlashAttribute("msjLayout", "error;Error;No tiene permiso para editar este usuario.");
			return "redirect:/listadoDeportistas";
		}

		Club club = clubService.findById(idClubSession);
		usuario.setClub(club);

		usuario.setEstado(usuario.getEnabled() ? "1" : "2");

		boolean esStaff = esUsuarioStaff(usuario);
		String tipoSel = TipoAltaUsuarioClub.APODERADO;
		if (esStaff) {
			if (usuario.getRoles().stream().anyMatch(r -> AppRoles.TESORERO.equals(r.getAuthority()))) {
				tipoSel = TipoAltaUsuarioClub.TESORERO;
			} else if (usuario.getRoles().stream().anyMatch(r -> AppRoles.ENTRENADOR.equals(r.getAuthority()))) {
				tipoSel = TipoAltaUsuarioClub.ENTRENADOR;
			}
		}

		model.addAttribute("titulo", esStaff ? "Actualizar personal del club" : "Actualizar Datos Usuario");
		model.addAttribute("usuario", usuario);
		model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
		model.addAttribute("estados", estados());
		model.addAttribute("isEditing", true);
		model.addAttribute("btn", "Actualizar");
		model.addAttribute("puedeCrearStaff", esRolClub(authentication));
		model.addAttribute("tipoAltaSeleccionado", tipoSel);
		model.addAttribute("esStaffCreacion", false);
		model.addAttribute("esStaff", esStaff);

		return "usuario";
	}

	@GetMapping("/eliminarUsuario/{id}")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
	public String eliminarUsuarioApoderado(@PathVariable Long id, HttpServletRequest request, RedirectAttributes flash,
			Authentication authentication) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		Usuario u = usuarioService.findByIdWithRolesAndDeportistas(id);
		if (u == null || u.getClub() == null || !idClubSession.equals(u.getClub().getId())) {
			flash.addFlashAttribute("msjLayout", "error;Error;No se pudo eliminar el usuario.");
			return "redirect:/listadoDeportistas";
		}

		boolean esApoderado = u.getRoles().stream().anyMatch(r -> AppRoles.USER.equals(r.getAuthority()));
		boolean esStaff = esUsuarioStaff(u);

		if (esStaff) {
			if (!esRolClub(authentication)) {
				flash.addFlashAttribute("msjLayout", "error;Error;Solo el administrador del club puede eliminar a este usuario.");
				return "redirect:/listadoDeportistas";
			}
			try {
				usuarioService.delete(u);
				flash.addFlashAttribute("msjLayout", "success;Eliminado;El usuario fue eliminado correctamente.");
			} catch (Exception e) {
				flash.addFlashAttribute("msjLayout",
						"error;Error;No se pudo eliminar. Puede haber información asociada.");
			}
			return "redirect:/listadoDeportistas";
		}

		if (!esApoderado) {
			flash.addFlashAttribute("msjLayout", "error;Error;Operación no permitida.");
			return "redirect:/listadoDeportistas";
		}

		try {
			usuarioService.delete(u);
			flash.addFlashAttribute("msjLayout", "success;Eliminado;El usuario fue eliminado correctamente.");
		} catch (Exception e) {
			flash.addFlashAttribute("msjLayout",
					"error;Error;No se pudo eliminar. Puede haber información asociada (pagos, deportistas, etc.).");
		}

		return "redirect:/listadoDeportistas";
	}

	@GetMapping("/club/logo/{id}")
	@ResponseBody
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO", "ROLE_ENTRENADOR" })
	public ResponseEntity<byte[]> verLogo(@PathVariable Long id, HttpServletRequest request, Principal principal) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		Usuario u = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (u == null || u.getClub() == null || idClubSession == null || !id.equals(idClubSession)
				|| !id.equals(u.getClub().getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		Club club = clubService.findById(id);
		if (club == null || club.getLogo() == null) {
			return ResponseEntity.notFound().build();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);

		return new ResponseEntity<>(club.getLogo(), headers, HttpStatus.OK);
	}

	@GetMapping("/perfilClub")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
	public String miPerfil(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {

		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		Club club = clubService.findById(usuario.getClub().getId());

		model.addAttribute("titulo", "Mantenedor Club");
		model.addAttribute("club", club);
		model.addAttribute("usuario", usuario);
		model.addAttribute("btn", "Actualizar");
		model.addAttribute("btnVolver", "/listadoDeportistas");

		return "club";
	}
	
	@PostMapping("/guardarPerfilClub")
	@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
	public String guardarPerfilClub(@Valid @ModelAttribute("club") Club club,
	                                 BindingResult result,
	                                 Model model,
	                                 @RequestParam("email") String email,
	                                 SessionStatus status,
	                                 RedirectAttributes flash,
	                                 HttpServletRequest request,
	                                 Principal principal,
	                                 @RequestParam(required = false) MultipartFile fileLogo,
	                                 @RequestParam(defaultValue = "false") boolean eliminarLogo) {


		model.addAttribute("titulo", "Mantenedor Club");
		model.addAttribute("btn", "Actualizar");
		model.addAttribute("btnVolver", "/listadoDeportistas");
	    
		if (result.hasErrors()) {
	        return "club";
	    }

	    Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
	    Club clubBD = clubService.findById(idClubSession);

	    if (clubBD == null) {
	        flash.addFlashAttribute("msjLayout", "danger;Error;Club no encontrado");
	        return "redirect:/perfilClub";
	    }
	    
	    if (email == null || email.isBlank()) {
	        result.reject("email", "El correo electrónico no puede estar vacío");
	        model.addAttribute("msjLayout", "error;Correo inválido;Debe ingresar un correo electrónico.");
	        return "club";
	    }
	    
	    Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
	    if (usuario == null) {
	        flash.addFlashAttribute("msjLayout", "danger;Sesión;No se pudo validar tu usuario.");
	        return "redirect:/perfilClub";
	    }
	    if (usuarioService.existsOtroUsuarioConEmailEnClub(email, idClubSession, usuario.getId())) {
			result.reject("email", "Ya existe un usuario registrado con este correo electrónico");
			model.addAttribute("msjLayout", "error;Correo duplicado;Ya existe un usuario registrado con ese email.");
			return "club";
		}

	    // Actualizar datos
	    clubBD.setNombre(club.getNombre());
	    clubBD.setTipo(club.getTipo());
	    clubBD.setEstado(club.getEstado());
	    clubBD.setDiaVencimientoCuota(club.getDiaVencimientoCuota());
	    
	    usuario.setEmail(email);

	    try {
	        // Eliminar logo si corresponde
	        if (eliminarLogo) {
	            clubBD.setLogo(null);
	        }

	        // Guardar nuevo logo si viene
	        if (fileLogo != null && !fileLogo.isEmpty()) {
	            clubBD.setLogo(fileLogo.getBytes());
	        }

	    } catch (Exception e) {
	        result.reject("logo", "Error al procesar la imagen");
	        return "club";
	    }

	    clubService.save(clubBD,usuario);
	    usuarioService.save(usuario);
	    status.setComplete();

	    flash.addFlashAttribute("msjLayout", "success;Club actualizado;Datos guardados correctamente");
	    return "redirect:/perfilClub";
	}

	private boolean esRolClub(Authentication authentication) {
		if (authentication == null) {
			return false;
		}
		return authentication.getAuthorities().stream().anyMatch(a -> AppRoles.CLUB.equals(a.getAuthority()));
	}

	private boolean esUsuarioStaff(Usuario u) {
		if (u == null || u.getRoles() == null) {
			return false;
		}
		return u.getRoles().stream().anyMatch(r -> AppRoles.TESORERO.equals(r.getAuthority())
				|| AppRoles.ENTRENADOR.equals(r.getAuthority()));
	}

	private void copiarDatosBasicos(Usuario desde, Usuario hacia) {
		hacia.setNombre(desde.getNombre());
		hacia.setApellido(desde.getApellido());
		hacia.setEmail(desde.getEmail());
		hacia.setTelefono(desde.getTelefono());
		hacia.setDireccion(desde.getDireccion());
		hacia.setRut(desde.getRut());
		hacia.setEstado(desde.getEstado());
	}

	private void validarDeportistasApoderado(Usuario usuario, BindingResult result) {
		if (usuario.getDeportistas() == null || usuario.getDeportistas().isEmpty()) {
			result.reject("deportistas", "Debe registrar al menos un deportista.");
		}
	}

	private void validarUsuario(Usuario usuario, BindingResult result) {
		if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
			result.rejectValue("apellido", "NotEmpty", "El apellido es obligatorio");
		}
		if (usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
			result.rejectValue("telefono", "NotEmpty", "El teléfono es obligatorio");
		}
		if (usuario.getDireccion() == null || usuario.getDireccion().isBlank()) {
			result.rejectValue("direccion", "NotEmpty", "La dirección es obligatoria");
		}
		if (usuario.getRut() == null || usuario.getRut().isBlank()) {
			result.rejectValue("rut", "NotEmpty", "El RUT es obligatorio");
		}
	}

	@ModelAttribute("estados")
	public Map<String, String> estados() {
		Map<String, String> estados = new HashMap<String, String>();
		estados.put("", "Seleccione");
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
