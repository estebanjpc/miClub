package com.app.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Categoria;
import com.app.entity.Club;
import com.app.entity.Deportista;
import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.service.ICategoriaService;
import com.app.service.IClubService;
import com.app.service.IEmailService;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@SessionAttributes("usuario")
public class UsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@Autowired
	private IEmailService emailService;
	
	@Autowired
	private IClubService clubService;
	
	@Autowired
	private ICategoriaService categoriaService;

	@RequestMapping(value = { "/listadoUsuarios" })
	public String listadoUsuariosEmpresa(Model model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
		List<Usuario> listadoUsuarios = usuarioService.findUsuarioByIdClub(idClubSession,"ROLE_USER");
		model.addAttribute("titulo", "Listado Usuarios");
		model.addAttribute("listadoUsuarios", listadoUsuarios);
		return "listadoUsuarios";
	}

	@RequestMapping(value = { "/crearUsuario" })
	public String crearUsuario(Map<String, Object> model, RedirectAttributes flash, Authentication authentication,
			HttpServletRequest request) {

		Usuario usuarioLogin = (Usuario) request.getSession().getAttribute("usuarioLogin");
		Usuario usuario = new Usuario();
		usuario.setRoles(Arrays.asList(new Role("Usuario", "ROLE_USER")));
		usuario.setDeportistas(new ArrayList<>());
	    usuario.getDeportistas().add(new Deportista());
	    List<Categoria> lista = categoriaService.findByClub(usuarioLogin.getClub());

		model.put("titulo", "Crear Usuario / Apoderado");
		model.put("usuario", usuario);
		model.put("categorias", lista);
	    model.put("estados", estados());
		model.put("isEditing", false);
		model.put("btn", "Crear");
		return "usuario";
	}

	@PostMapping("/guardarUsuario")
	public String guardarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,BindingResult result,Model model,SessionStatus status,RedirectAttributes flash,HttpServletRequest request) {

	    // Validaciones manuales del apoderado
	    if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
	        result.rejectValue("apellido", "NotEmpty", "El apellido es obligatorio");
	    }
	    if (usuario.getTelefono() == null || usuario.getTelefono().trim().isEmpty()) {
	        result.rejectValue("telefono", "NotEmpty", "El teléfono es obligatorio");
	    }
	    if (usuario.getDireccion() == null || usuario.getDireccion().trim().isEmpty()) {
	        result.rejectValue("direccion", "NotEmpty", "La dirección es obligatoria");
	    }
	    if (usuario.getRut() == null || usuario.getRut().trim().isEmpty()) {
	        result.rejectValue("rut", "NotEmpty", "El RUT es obligatorio");
	    }

	    //NUEVO: Filtra deportistas completamente vacíos (no ingresaron nada)
	    if (usuario.getDeportistas() != null) {
	        usuario.setDeportistas(
	            usuario.getDeportistas().stream()
	                .filter(d ->
	                    (d.getNombre() != null && !d.getNombre().trim().isEmpty()) ||
	                    (d.getApellido() != null && !d.getApellido().trim().isEmpty()) ||
	                    (d.getRut() != null && !d.getRut().trim().isEmpty()) ||
	                    d.getFechaNacimiento() != null ||
	                    (d.getSexo() != null && !d.getSexo().trim().isEmpty()) ||
	                    (d.getCategoria() != null && !d.getCategoria().trim().isEmpty())
	                )
	                .toList()
	        );
	    }
	    
	    boolean flag = (usuario.getId() == null);
		String pass = "";
		String msje = "Editado con exito";

	    // Si hay errores, volver al formulario
	    if (result.hasErrors()) {
	        model.addAttribute("titulo", "Crear Usuario / Apoderado");
	        model.addAttribute("btn", "Guardar");
	        return "usuario";
	    }

	    // Setear la relación usuario → deportistas
	    if (usuario.getDeportistas() != null && !usuario.getDeportistas().isEmpty()) {
	        for (Deportista d : usuario.getDeportistas()) {
	            d.setUsuario(usuario);
	        }
	    }

	    Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
	    Club club = clubService.findById(idClubSession);
	    usuario.setClub(club);
	    
	    usuario.setEnabled((usuario.getEstado().equalsIgnoreCase("1")) ? true : false);
	    
	    if (flag) {
			pass = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(pass));
			usuario.setEstado("0");
			msje = "Creado con exito";
		}
	    
	    usuarioService.save(usuario);
	    status.setComplete();
	    
	    if (flag) {
			usuario.setPassAux(pass);
			Executors.newSingleThreadExecutor().execute(() -> emailService.creacionUsuario(usuario));
		}
	    
	    flash.addFlashAttribute("msjLayout", "success;" + msje + "!;Usuario " + msje);
	    
	    return "redirect:/listadoUsuarios";
	}
	
	@RequestMapping(value = { "/editarUsuario/{id}" })
	public String editarUsuario(@PathVariable(value = "id") Long id,
			Map<String, Object> model,
			RedirectAttributes flash, 
			Authentication authentication, 
			HttpServletRequest request) {

		Usuario usuarioLogin = (Usuario) request.getSession().getAttribute("usuarioLogin");
		Usuario usuario = usuarioService.findById(id);

		if (null == usuario) {
			model.put("msjLayout", "error;Usuario no existe;Usuario no existe en BD");
			return "redirect:/listadoUsuarios";
		}

		
		usuario.setEstado((usuario.getEnabled()) ? "1" : "2");

		model.put("titulo", "Actualizar Datos Usuario");
		model.put("usuario", usuario);
		model.put("categorias", categoriaService.findByClub(usuarioLogin.getClub()));
	    model.put("estados", estados());
		model.put("isEditing", true);
		model.put("btn", "Actualizar");
		return "usuario";
	}
	
	
	@GetMapping("/club/logo/{id}")
	@ResponseBody
	public ResponseEntity<byte[]> verLogo(@PathVariable Long id) {
	    Club club = clubService.findById(id);
	    if (club == null || club.getLogo() == null) {
	        return ResponseEntity.notFound().build();
	    }
	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.IMAGE_JPEG); // o IMAGE_PNG según el formato
	    return new ResponseEntity<>(club.getLogo(), headers, HttpStatus.OK);
	}
	
	@RequestMapping(value = { "/perfilClub" })
	public String miPerfil(Map<String, Object> model, RedirectAttributes flash, Authentication authentication, HttpServletRequest request) {
		Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogin");
		model.put("titulo", "Mantenedor Club");
		model.put("usuario", usuario);
		model.put("btn", "Actualizar");
		model.put("btnVolver", "/listadoUsuarios");
		return "club";
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
