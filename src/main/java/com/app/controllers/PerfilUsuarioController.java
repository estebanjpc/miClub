package com.app.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.dto.DeportistaApoderadoForm;
import com.app.dto.ListaDeportistasApoderadoForm;
import com.app.entity.Deportista;
import com.app.entity.Usuario;
import com.app.service.IDeportistaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@Secured({ "ROLE_USER", "ROLE_SOCIO" })
public class PerfilUsuarioController {

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private IDeportistaService deportistaService;

	@GetMapping("/perfilUsuario")
	public String perfilUsuario(Model model, Principal principal, HttpServletRequest request,
			@RequestParam(name = "tab", defaultValue = "datos") String tab) {
		Usuario actual = usuarioService.resolveUsuarioActivo(principal.getName(),
				(Long) request.getSession().getAttribute("idClubSession"));
		if (actual == null) {
			return "redirect:/seleccionarClub";
		}

		cargarModeloPerfil(model, actual, tab);
		return "perfilUsuario";
	}

	private void cargarModeloPerfil(Model model, Usuario actual, String tab) {
		Usuario usuarioVista = usuarioService.findById(actual.getId());
		if (usuarioVista == null) {
			usuarioVista = actual;
		}

		model.addAttribute("titulo", "Mi perfil");
		model.addAttribute("usuario", usuarioVista);
		model.addAttribute("tab", tab);

		if (usuarioVista.getClub() != null) {
			model.addAttribute("nombreClub", usuarioVista.getClub().getNombre());
		}

		List<Deportista> deps = deportistaService.listarPorUsuario(usuarioVista.getId());
		model.addAttribute("deportistasLista", deps);
		model.addAttribute("deportistasCmd", ListaDeportistasApoderadoForm.desdeEntidades(deps));
	}

	/** Mantiene el {@code usuario} del binding (valores enviados) y carga la pestaña deportistas. */
	private void prepararTabsTrasErrorDatosPersonales(Model model, Usuario actual) {
		model.addAttribute("titulo", "Mi perfil");
		model.addAttribute("tab", "datos");
		if (actual.getClub() != null) {
			model.addAttribute("nombreClub", actual.getClub().getNombre());
		}
		List<Deportista> deps = deportistaService.listarPorUsuario(actual.getId());
		model.addAttribute("deportistasLista", deps);
		model.addAttribute("deportistasCmd", ListaDeportistasApoderadoForm.desdeEntidades(deps));
	}

	@PostMapping("/guardarPerfilUsuario")
	public String guardarPerfil(@ModelAttribute("usuario") Usuario form, BindingResult result, Model model,
			Principal principal, HttpServletRequest request, RedirectAttributes flash) {

		Usuario actual = usuarioService.resolveUsuarioActivo(principal.getName(),
				(Long) request.getSession().getAttribute("idClubSession"));
		if (actual == null) {
			flash.addFlashAttribute("msjLayout", "danger;Sesión;No se pudo identificar tu usuario.");
			return "redirect:/seleccionarClub";
		}

		if (!Objects.equals(actual.getId(), form.getId())) {
			flash.addFlashAttribute("msjLayout", "danger;Error;Solicitud no válida.");
			return "redirect:/perfilUsuario";
		}

		validarDatosPersonales(form, result);

		if (result.hasErrors()) {
			prepararTabsTrasErrorDatosPersonales(model, actual);
			return "perfilUsuario";
		}

		String emailNuevo = form.getEmail() != null ? form.getEmail().trim() : "";
		String emailLogin = principal.getName();
		List<Usuario> mismoEmail = new ArrayList<>(usuarioService.findAllByEmail(emailLogin));

		for (Usuario u : mismoEmail) {
			Long cid = u.getClub() != null ? u.getClub().getId() : null;
			if (cid != null && usuarioService.existsOtroUsuarioConEmailEnClub(emailNuevo, cid, u.getId())) {
				result.rejectValue("email", "duplicate", "Ya existe otro usuario con este correo en uno de tus clubes.");
				prepararTabsTrasErrorDatosPersonales(model, actual);
				return "perfilUsuario";
			}
		}

		actual.setNombre(form.getNombre());
		actual.setApellido(form.getApellido());
		actual.setEmail(emailNuevo);
		actual.setTelefono(form.getTelefono());
		actual.setDireccion(form.getDireccion());
		actual.setRut(form.getRut());

		usuarioService.save(actual);

		if (!emailNuevo.equals(emailLogin)) {
			for (Usuario u : mismoEmail) {
				if (!u.getId().equals(actual.getId())) {
					Usuario otro = usuarioService.findById(u.getId());
					if (otro != null) {
						otro.setEmail(emailNuevo);
						usuarioService.save(otro);
					}
				}
			}
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				UsernamePasswordAuthenticationToken nueva = new UsernamePasswordAuthenticationToken(emailNuevo,
						auth.getCredentials(), auth.getAuthorities());
				nueva.setDetails(auth.getDetails());
				SecurityContextHolder.getContext().setAuthentication(nueva);
			}
		}

		request.getSession().setAttribute("usuarioLogin", usuarioService.findById(actual.getId()));

		flash.addFlashAttribute("msjLayout", "success;Perfil actualizado;Tus datos se guardaron correctamente.");
		return "redirect:/perfilUsuario?tab=datos";
	}

	@PostMapping("/guardarPerfilDeportistas")
	public String guardarPerfilDeportistas(@ModelAttribute("deportistasCmd") ListaDeportistasApoderadoForm cmd,
			Principal principal, HttpServletRequest request, RedirectAttributes flash, Model model) {

		Usuario actual = usuarioService.resolveUsuarioActivo(principal.getName(),
				(Long) request.getSession().getAttribute("idClubSession"));
		if (actual == null) {
			flash.addFlashAttribute("msjLayout", "danger;Sesión;No se pudo identificar tu usuario.");
			return "redirect:/seleccionarClub";
		}

		if (cmd.getFilas() == null || cmd.getFilas().isEmpty()) {
			flash.addFlashAttribute("msjLayout", "info;Sin deportistas;No hay deportistas para actualizar.");
			return "redirect:/perfilUsuario?tab=deportistas";
		}

		for (DeportistaApoderadoForm ficha : cmd.getFilas()) {
			String err = validarDeportistaApoderado(ficha);
			if (err != null) {
				model.addAttribute("deportistaPerfilError", err);
				cargarModeloPerfil(model, actual, "deportistas");
				model.addAttribute("deportistasCmd", cmd);
				return "perfilUsuario";
			}
		}

		Long uid = actual.getId();
		for (DeportistaApoderadoForm ficha : cmd.getFilas()) {
			if (ficha.getId() == null) {
				continue;
			}
			Deportista dep = deportistaService.findById(ficha.getId());
			if (dep == null || dep.getUsuario() == null || !uid.equals(dep.getUsuario().getId())) {
				flash.addFlashAttribute("msjLayout", "danger;Error;No se pudo validar un deportista.");
				return "redirect:/perfilUsuario?tab=deportistas";
			}

			dep.setNombre(ficha.getNombre().trim());
			dep.setApellido(ficha.getApellido().trim());
			dep.setRut(ficha.getRut().trim());
			dep.setFechaNacimiento(ficha.getFechaNacimiento());
			dep.setSexo(ficha.getSexo());

			deportistaService.save(dep);
		}

		request.getSession().setAttribute("usuarioLogin", usuarioService.findById(uid));

		flash.addFlashAttribute("msjLayout", "success;Deportistas actualizados;Los datos se guardaron correctamente.");
		return "redirect:/perfilUsuario?tab=deportistas";
	}

	@ModelAttribute("estadosDeportista")
	public Map<String, String> estadosDeportista() {
		Map<String, String> m = new HashMap<>();
		m.put("1", "Activo");
		m.put("2", "Desactivo");
		return m;
	}

	private String validarDeportistaApoderado(DeportistaApoderadoForm f) {
		if (f.getNombre() == null || f.getNombre().isBlank()) {
			return "El nombre del deportista es obligatorio.";
		}
		if (f.getApellido() == null || f.getApellido().isBlank()) {
			return "El apellido del deportista es obligatorio.";
		}
		if (f.getRut() == null || f.getRut().isBlank()) {
			return "El RUT del deportista es obligatorio.";
		}
		if (f.getFechaNacimiento() == null) {
			return "La fecha de nacimiento es obligatoria.";
		}
		if (f.getSexo() == null || f.getSexo().isBlank()) {
			return "Debe indicar el sexo del deportista.";
		}
		return null;
	}

	private void validarDatosPersonales(Usuario usuario, BindingResult result) {
		if (usuario.getNombre() == null || usuario.getNombre().isBlank()) {
			result.rejectValue("nombre", "NotEmpty", "El nombre es obligatorio");
		}
		if (usuario.getApellido() == null || usuario.getApellido().isBlank()) {
			result.rejectValue("apellido", "NotEmpty", "El apellido es obligatorio");
		}
		if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
			result.rejectValue("email", "NotEmpty", "El correo electrónico es obligatorio");
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
}
