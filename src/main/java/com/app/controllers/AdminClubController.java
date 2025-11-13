package com.app.controllers;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Role;
import com.app.entity.Usuario;
import com.app.service.IClubService;
import com.app.service.IEmailService;
import com.app.service.IUsuarioService;
import com.app.util.Util;

import jakarta.validation.Valid;

@Controller
@Secured("ROLE_ADMIN")
@SessionAttributes("usuario")
public class AdminClubController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IEmailService emailService;

	@Autowired
	private IClubService clubService;

	@Autowired
	private BCryptPasswordEncoder passEncoder;

	@RequestMapping(value = { "/listadoClub" })
	public String mantenedorUsuario(Model model) {
		List<Usuario> listadoClub = usuarioService.findUsuarioByAuthority("ROLE_CLUB");
		model.addAttribute("titulo", "Mantenedor Club");
		model.addAttribute("listadoClub", listadoClub);
		return "listadoClub";
	}

	@RequestMapping(value = { "/crearClub" })
	public String crearClub(Map<String, Object> model) {
		Usuario usuario = new Usuario();
		usuario.setRoles(Arrays.asList(new Role("Club", "ROLE_CLUB")));
		model.put("titulo", "Mantenedor Club");
		model.put("usuario", usuario);
		model.put("btn", "Crear");
		return "club";
	}

	@PostMapping("/guardarClub")
	public String guardarUsuario(@Valid Usuario usuario, BindingResult result,
			@RequestParam(value = "fileLogo", required = false) MultipartFile fileLogo,
			@RequestParam(value = "eliminarLogo", required = false) String eliminarLogo,
			Map<String, Object> model, RedirectAttributes flash, SessionStatus status) {

		model.put("titulo", "Mantenedor Club");
		model.put("btn", "Actualizar");

		String msje = "Editado con Éxito";
		String pass = "";
		boolean flagNuevo = false;

		if (result.hasErrors())
			return "club";

		Usuario aux = usuarioService.findByEmail(usuario.getEmail());
		if (aux != null && !aux.getId().equals(usuario.getId())) {
			result.rejectValue("email", "error.usuario", "Ya existe un usuario registrado con este correo electrónico");
			model.put("msjLayout", "error;Correo duplicado;Ya existe un usuario registrado con ese email.");
			return "club";
		}

		if ("true".equals(eliminarLogo)) {
	        usuario.getClub().setLogo(null);
	    } else if (fileLogo != null && !fileLogo.isEmpty()) {
	        try {
	        	usuario.getClub().setLogo(fileLogo.getBytes());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		
		// NUEVO usuario
		if (usuario.getId() == null) {
			flagNuevo = true;
			pass = Util.generatePassword(10);
			usuario.setPassword(passEncoder.encode(pass));
			usuario.setEstado("0"); // debe cambiar clave al ingresar
			msje = "Creado con Éxito";
			model.put("btn", "Crear");
			
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
				usuario.setEstado(existente.getEstado());
				usuario.setPassword(existente.getPassword());
				usuario.getClub().setCodigo(existente.getClub().getCodigo());
			}
		}

		usuario.setEnabled("1".equals(usuario.getEstado()));
		usuario.getClub().setEstado(usuario.getEstado());
		usuario.getClub().setNombre(usuario.getNombre());

//		clubService.save(usuario.getClub());
		usuarioService.save(usuario);
		status.setComplete();
		flash.addFlashAttribute("msjLayout", "success;" + msje + "!;Club " + msje);

		if (flagNuevo) {
			usuario.setPassAux(pass);
			Executors.newSingleThreadExecutor().execute(() -> emailService.creacionClub(usuario));
		}

		return "redirect:/listadoClub";
	}

	@RequestMapping(value = { "/editarClub/{id}" })
	public String editarClub(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
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
