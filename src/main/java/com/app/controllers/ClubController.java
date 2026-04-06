package com.app.controllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.AsyncEmailService;
import com.app.service.IDeportistaService;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("usuario")
@Secured("ROLE_CLUB")
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

	@GetMapping("/listadoUsuarios")
	public String listadoUsuariosRedirect() {
		return "redirect:/listadoDeportistas";
	}

	@GetMapping("/listadoDeportistas")
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

		model.addAttribute("titulo", "Listado de deportistas");
		model.addAttribute("listadoDeportistas", listadoDeportistas);
		return "listadoDeportistas";
	}

	@GetMapping("/crearUsuario")
	public String crearUsuario(Model model, HttpServletRequest request, Principal principal,
			RedirectAttributes flash) {

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		Usuario usuario = new Usuario();
		usuario.setRoles(Arrays.asList(new Role("Usuario", "ROLE_USER")));
		usuario.setDeportistas(new ArrayList<>());
		usuario.getDeportistas().add(new Deportista());

		model.addAttribute("titulo", "Crear Usuario / Apoderado");
		model.addAttribute("usuario", usuario);
		model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
		model.addAttribute("estados", estados());
		model.addAttribute("isEditing", false);
		model.addAttribute("btn", "Crear");

		return "usuario";
	}

	@PostMapping("/guardarUsuarioClub")
	public String guardarUsuarioClub(@Valid @ModelAttribute("usuario") Usuario usuario, BindingResult result, Model model,
			SessionStatus status, RedirectAttributes flash, HttpServletRequest request, Principal principal) {

		validarUsuario(usuario, result);

		// Filtrar deportistas vacíos
		if (usuario.getDeportistas() != null) {
			usuario.setDeportistas(usuario.getDeportistas().stream()
					.filter(d -> (d.getNombre() != null && !d.getNombre().isBlank())
							|| (d.getApellido() != null && !d.getApellido().isBlank())
							|| (d.getRut() != null && !d.getRut().isBlank()) || d.getFechaNacimiento() != null
							|| (d.getSexo() != null && !d.getSexo().isBlank()) || d.getCategoria() != null)
					.collect(java.util.stream.Collectors.toList()));
		}


		if (result.hasErrors()) {
			Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
			if (usuarioLogin == null) {
				flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
				return "redirect:/seleccionarClub";
			}

			model.addAttribute("titulo", "Crear Usuario / Apoderado");
			model.addAttribute("btn", "Guardar");
			model.addAttribute("isEditing", usuario.getId() != null);
			model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
			model.addAttribute("estados", estados());

			return "usuario";
		}

		boolean nuevo = (usuario.getId() == null);
		String passGenerada = null;
		String mensaje = nuevo ? "Creado" : "Editado";

		// Relación deportista → usuario
		if (usuario.getDeportistas() != null) {
		    usuario.getDeportistas().forEach(d -> {
		        d.setUsuario(usuario);

		        if (d.getFechaIngreso() == null) {
		            d.setFechaIngreso(LocalDate.now());
		        }
		    });
		}

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		Club club = clubService.findById(idClubSession);
		usuario.setClub(club);

		usuario.setEnabled("1".equals(usuario.getEstado()));

		if (nuevo) {
			passGenerada = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(passGenerada));
			usuario.setEstado("0");
			usuario.getRoles().get(0).setUsuario(usuario);
		}
		
		usuarioService.save(usuario);
		status.setComplete();

		if (nuevo) {
			usuario.setPassAux(passGenerada);
			asyncEmailService.creacionUsuario(usuario);
		}

		flash.addFlashAttribute("msjLayout", "success;" + mensaje + " con éxito;Usuario " + mensaje);

		return "redirect:/listadoDeportistas";
	}

	@GetMapping("/editarUsuario/{id}")
	public String editarUsuario(@PathVariable Long id, Model model, HttpServletRequest request, Principal principal,
			RedirectAttributes flash) {

		Usuario usuarioLogin = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuarioLogin == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}
		Usuario usuario = usuarioService.findById(id);

		if (usuario == null) {
			return "redirect:/listadoDeportistas";
		}
		
		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		Club club = clubService.findById(idClubSession);
		usuario.setClub(club);

		usuario.setEstado(usuario.getEnabled() ? "1" : "2");

		model.addAttribute("titulo", "Actualizar Datos Usuario");
		model.addAttribute("usuario", usuario);
		model.addAttribute("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
		model.addAttribute("estados", estados());
		model.addAttribute("isEditing", true);
		model.addAttribute("btn", "Actualizar");

		return "usuario";
	}

	@GetMapping("/eliminarUsuario/{id}")
	public String eliminarUsuarioApoderado(@PathVariable Long id, HttpServletRequest request, RedirectAttributes flash) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		if (idClubSession == null) {
			return "redirect:/login";
		}

		Usuario u = usuarioService.findById(id);
		if (u == null || u.getClub() == null || !idClubSession.equals(u.getClub().getId())) {
			flash.addFlashAttribute("msjLayout", "error;Error;No se pudo eliminar el usuario.");
			return "redirect:/listadoDeportistas";
		}

		boolean esApoderado = u.getRoles().stream().anyMatch(r -> "ROLE_USER".equals(r.getAuthority()));
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
	public ResponseEntity<byte[]> verLogo(@PathVariable Long id) {

		Club club = clubService.findById(id);
		if (club == null || club.getLogo() == null) {
			return ResponseEntity.notFound().build();
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);

		return new ResponseEntity<>(club.getLogo(), headers, HttpStatus.OK);
	}

	@GetMapping("/perfilClub")
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
		lista.put("", "Seleccione");
		lista.put("Baquetball", "Baquetball");
		lista.put("Futbol", "Futbol");
		lista.put("Pin-Pong", "Pin-Pong");
		lista.put("MultiDeportes", "MultiDeportes");
		return lista;
	}
}
