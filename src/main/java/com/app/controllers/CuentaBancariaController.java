package com.app.controllers;

import java.security.Principal;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.util.StringUtils;

import com.app.editors.BancoEditor;
import com.app.entity.Banco;
import com.app.entity.CuentaBancaria;
import com.app.entity.Usuario;
import com.app.service.ClubMediosPagoService;
import com.app.service.IBancoService;
import com.app.service.ICuentaBancariaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class CuentaBancariaController {

	@Autowired
	private ICuentaBancariaService cuentaService;

	@Autowired
	private IBancoService bancoService;

	@Autowired
	private BancoEditor bancoEditor;

	@Autowired
	private IUsuarioService usuarioService;

	@Autowired
	private ClubMediosPagoService clubMediosPagoService;

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Banco.class, "banco", bancoEditor);
	}

	@GetMapping("/cuentas")
	public String resumen(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		CuentaBancaria cuenta = cuentaService.findByClub(usuario.getClub());
		boolean transferenciaOk = clubMediosPagoService.cuentaBancariaCompletaParaTransferencias(cuenta);
		boolean khipuEnBd = cuenta != null && StringUtils.hasText(cuenta.getKhipuApiKey());
		boolean khipuCobroListo = clubMediosPagoService.khipuConfiguradoParaCobros(cuenta);

		model.addAttribute("titulo", "Pagos y cobros del club");
		model.addAttribute("activeTab", "resumen");
		model.addAttribute("cuenta", cuenta);
		model.addAttribute("transferenciaOk", transferenciaOk);
		model.addAttribute("khipuEnBd", khipuEnBd);
		model.addAttribute("khipuCobroListo", khipuCobroListo);
		return "cuentasResumen";
	}

	@GetMapping("/cuentas/cuentaBancaria")
	public String formCuentaBancaria(Model model, HttpServletRequest request, Principal principal,
			RedirectAttributes flash) {
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		CuentaBancaria cuenta = cuentaService.findByClub(usuario.getClub());
		boolean tieneCuenta = cuenta != null;
		CuentaBancaria cuentaForm = new CuentaBancaria();
		if (tieneCuenta) {
			BeanUtils.copyProperties(cuenta, cuentaForm);
			normalizarTipoCuentaParaSelect(cuentaForm);
		}

		model.addAttribute("titulo", "Cuenta bancaria para transferencias");
		model.addAttribute("activeTab", "bancaria");
		model.addAttribute("listadoBancos", bancoService.findAll());
		model.addAttribute("tieneCuenta", tieneCuenta);
		model.addAttribute("cuenta", cuentaForm);
		return "cuentaBancariaForm";
	}

	@PostMapping("/cuentas/guardar-bancaria")
	public String guardarBancaria(@ModelAttribute("cuenta") CuentaBancaria incoming, BindingResult result, Model model,
			HttpServletRequest request, Principal principal, RedirectAttributes flash) {

		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		normalizarTipoCuentaParaSelect(incoming);
		validarSoloBanco(incoming, result);
		model.addAttribute("listadoBancos", bancoService.findAll());
		model.addAttribute("titulo", "Cuenta bancaria para transferencias");
		model.addAttribute("activeTab", "bancaria");

		if (result.hasErrors()) {
			CuentaBancaria existente = cuentaService.findByClub(usuario.getClub());
			model.addAttribute("tieneCuenta", existente != null);
			model.addAttribute("cuenta", incoming);
			return "cuentaBancariaForm";
		}

		CuentaBancaria guardar = cuentaService.findByClub(usuario.getClub());
		if (guardar == null) {
			guardar = new CuentaBancaria();
			guardar.setClub(usuario.getClub());
		}
		guardar.setBanco(incoming.getBanco());
		guardar.setTipoCuenta(incoming.getTipoCuenta());
		guardar.setNumeroCuenta(incoming.getNumeroCuenta());
		guardar.setNombreTitular(incoming.getNombreTitular());
		guardar.setRut(incoming.getRut());
		guardar.setEmail(incoming.getEmail());

		cuentaService.save(guardar);
		flash.addFlashAttribute("msjLayout", "success;Cuenta bancaria;Datos guardados correctamente.");
		return "redirect:/cuentas";
	}

	@GetMapping("/cuentas/khipu")
	public String formKhipu(Model model, HttpServletRequest request, Principal principal, RedirectAttributes flash) {
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		CuentaBancaria cuenta = cuentaService.findByClub(usuario.getClub());
		model.addAttribute("titulo", "Integración Khipu");
		model.addAttribute("activeTab", "khipu");
		model.addAttribute("cuenta", cuenta != null ? cuenta : new CuentaBancaria());
		model.addAttribute("tieneRegistro", cuenta != null);
		return "cuentaKhipuForm";
	}

	@PostMapping("/cuentas/guardar-khipu")
	public String guardarKhipu(@ModelAttribute("cuenta") CuentaBancaria incoming, HttpServletRequest request,
			Principal principal, RedirectAttributes flash) {

		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		CuentaBancaria guardar = cuentaService.findByClub(usuario.getClub());
		if (guardar == null) {
			guardar = new CuentaBancaria();
			guardar.setClub(usuario.getClub());
		} else {
			if (!StringUtils.hasText(incoming.getKhipuApiKey())) {
				incoming.setKhipuApiKey(guardar.getKhipuApiKey());
			}
			if (!StringUtils.hasText(incoming.getKhipuMerchantSecret())) {
				incoming.setKhipuMerchantSecret(guardar.getKhipuMerchantSecret());
			}
		}

		guardar.setKhipuApiUrl(incoming.getKhipuApiUrl());
		if (StringUtils.hasText(incoming.getKhipuApiKey())) {
			guardar.setKhipuApiKey(incoming.getKhipuApiKey().trim());
		}
		if (StringUtils.hasText(incoming.getKhipuMerchantSecret())) {
			guardar.setKhipuMerchantSecret(incoming.getKhipuMerchantSecret().trim());
		}

		cuentaService.save(guardar);
		flash.addFlashAttribute("msjLayout", "success;Khipu;Credenciales guardadas correctamente.");
		return "redirect:/cuentas";
	}

	@GetMapping("/cuentas/eliminar/{id}")
	public String eliminar(@PathVariable Long id, HttpServletRequest request, Principal principal,
			RedirectAttributes flash) {
		Usuario usuario = usuarioService.refrescarUsuarioSesion(request, principal.getName());
		if (usuario == null || usuario.getClub() == null) {
			flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
			return "redirect:/seleccionarClub";
		}

		CuentaBancaria cuenta = cuentaService.findById(id);
		if (cuenta != null && cuenta.getClub().getId().equals(usuario.getClub().getId())) {
			cuentaService.delete(id);
			flash.addFlashAttribute("msjLayout", "success;Eliminado;Registro de cuenta eliminado.");
		}

		return "redirect:/cuentas";
	}

	/**
	 * Alinea textos antiguos o abreviados (p. ej. "Cta Cte") con los {@code value} del {@code select} en la vista.
	 */
	private static void normalizarTipoCuentaParaSelect(CuentaBancaria c) {
		if (c == null || !StringUtils.hasText(c.getTipoCuenta())) {
			return;
		}
		String t = c.getTipoCuenta().trim();
		if ("Cta Cte".equalsIgnoreCase(t) || "Corriente".equalsIgnoreCase(t) || "Cuenta cte".equalsIgnoreCase(t)) {
			c.setTipoCuenta("Cuenta Corriente");
			return;
		}
		if ("Cta Vista".equalsIgnoreCase(t) || "Vista".equalsIgnoreCase(t)) {
			c.setTipoCuenta("Cuenta Vista");
			return;
		}
		if ("Cta Ahorro".equalsIgnoreCase(t) || "Ahorro".equalsIgnoreCase(t) || "Cta. Ahorro".equalsIgnoreCase(t)) {
			c.setTipoCuenta("Cuenta de Ahorro");
		}
	}

	private void validarSoloBanco(CuentaBancaria c, BindingResult result) {
		if (c.getBanco() == null) {
			result.rejectValue("banco", "required", "Debe seleccionar un banco");
		}
		if (!StringUtils.hasText(c.getTipoCuenta())) {
			result.rejectValue("tipoCuenta", "required", "El tipo de cuenta es obligatorio");
		}
		if (!StringUtils.hasText(c.getNumeroCuenta())) {
			result.rejectValue("numeroCuenta", "required", "El número de cuenta es obligatorio");
		}
		if (!StringUtils.hasText(c.getNombreTitular())) {
			result.rejectValue("nombreTitular", "required", "El titular es obligatorio");
		}
		if (!StringUtils.hasText(c.getRut())) {
			result.rejectValue("rut", "required", "El RUT es obligatorio");
		}
		if (!StringUtils.hasText(c.getEmail())) {
			result.rejectValue("email", "required", "El email es obligatorio");
		}
	}

}
