package com.app.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.app.editors.BancoEditor;
import com.app.entity.Banco;
import com.app.entity.CuentaBancaria;
import com.app.entity.Usuario;
import com.app.service.ICuentaBancariaService;
import com.app.service.IBancoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
//@SessionAttributes("cuenta")
public class CuentaBancariaController {

	@Autowired
	private ICuentaBancariaService cuentaService;

	@Autowired
	private IBancoService bancoService;
	
	@Autowired
	private BancoEditor bancoEditor;
	
	

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Banco.class, "banco",bancoEditor);
	}
	
	@GetMapping("/cuentas/cuentaBancaria")
	public String verCuenta(Model model, HttpServletRequest request) {
	    Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogin");
	    if (usuario == null || usuario.getClub() == null) {
	        return "redirect:/login";
	    }

	    CuentaBancaria cuenta = cuentaService.findByClub(usuario.getClub());
	    List<Banco> listadoBancos = bancoService.findAll();

	    boolean tieneCuenta = (cuenta != null);

	    model.addAttribute("titulo", "Cuenta Bancaria del Club");
	    model.addAttribute("listadoBancos", listadoBancos);
	    model.addAttribute("tieneCuenta", tieneCuenta);
	    model.addAttribute("cuenta", tieneCuenta ? cuenta : new CuentaBancaria());

	    return "cuenta";
	}



	@PostMapping("/cuentas/guardar")
	public String guardar(@Valid @ModelAttribute("cuenta") CuentaBancaria cuenta,
	                      BindingResult result,
	                      Model model,
	                      HttpServletRequest request) {

	    Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogin");
	    if (usuario == null || usuario.getClub() == null) {
	        return "redirect:/login";
	    }

	    List<Banco> listadoBancos = bancoService.findAll();
	    model.addAttribute("listadoBancos", listadoBancos);
	    model.addAttribute("titulo", "Cuenta Bancaria del Club");

	    if (result.hasErrors()) {
	        CuentaBancaria cuentaExistente = cuentaService.findByClub(usuario.getClub());
	        boolean tieneCuenta = (cuentaExistente != null);

	        model.addAttribute("tieneCuenta", tieneCuenta);
	        model.addAttribute("cuenta", cuenta);
	        return "cuenta";
	    }

	    cuenta.setClub(usuario.getClub());
	    cuentaService.save(cuenta);

	    return "redirect:/cuentas/cuentaBancaria";
	}


	/**
	 * Eliminar la cuenta bancaria existente
	 */
	@GetMapping("/cuentas/eliminar/{id}")
	public String eliminar(@PathVariable Long id, HttpServletRequest request) {
		Usuario usuario = (Usuario) request.getSession().getAttribute("usuarioLogin");
		if (usuario == null || usuario.getClub() == null) {
			return "redirect:/login";
		}

		CuentaBancaria cuenta = cuentaService.findById(id);
		if (cuenta != null && cuenta.getClub().getId().equals(usuario.getClub().getId())) {
			cuentaService.delete(id);
		}

		return "redirect:/cuentas/cuentaBancaria";
	}
}
