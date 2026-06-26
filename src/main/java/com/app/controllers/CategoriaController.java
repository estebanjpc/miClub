package com.app.controllers;

import java.security.Principal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.entity.Categoria;
import com.app.entity.CategoriaValorVigencia;
import com.app.entity.Club;
import com.app.entity.Usuario;
import com.app.service.ICategoriaService;
import com.app.service.ICategoriaCuotaVigenciaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
@Secured({ "ROLE_CLUB", "ROLE_TESORERO" })
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ICategoriaCuotaVigenciaService categoriaCuotaVigenciaService;

    @GetMapping("/listar")
    public String listar(Model model, Principal principal, HttpServletRequest request,
            RedirectAttributes flash) {
        Usuario usuario = usuarioClubActivo(principal, request);
        if (usuario == null) {
            flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
            return "redirect:/seleccionarClub";
        }
        Club club = usuario.getClub();

        List<Categoria> listadoCategorias = categoriaService.findByClub(club);
        model.addAttribute("titulo", "Listado de Categorías del Club " + club.getNombre());
        model.addAttribute("listadoCategorias", listadoCategorias);

        return "listadoCategorias";
    }

    @GetMapping("/crear")
    public String crear(Model model, Principal principal, HttpServletRequest request,
            RedirectAttributes flash) {
        Usuario usuario = usuarioClubActivo(principal, request);
        if (usuario == null) {
            flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
            return "redirect:/seleccionarClub";
        }
        Club club = usuario.getClub();

        Categoria categoria = new Categoria();
        categoria.setClub(club); // Se asocia automáticamente al club del usuario
        YearMonth ym = YearMonth.now();

        model.addAttribute("titulo", "Nueva Categoría");
        model.addAttribute("categoria", categoria);
        model.addAttribute("club", club);
        model.addAttribute("mesVigencia", ym.getMonthValue());
        model.addAttribute("anioVigencia", ym.getYear());
        model.addAttribute("vigencias", List.<CategoriaValorVigencia>of());

        return "categoria";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request,
            RedirectAttributes flash) {
        Usuario usuario = usuarioClubActivo(principal, request);
        if (usuario == null) {
            flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
            return "redirect:/seleccionarClub";
        }
        Club club = usuario.getClub();

        Categoria categoria = categoriaService.findById(id);
        if (categoria == null || !categoria.getClub().getId().equals(club.getId())) {
            flash.addFlashAttribute("error", "La categoría no existe o no pertenece a su club");
            return "redirect:/categorias/listar";
        }

        model.addAttribute("titulo", "Editar Categoría");
        model.addAttribute("categoria", categoria);
        model.addAttribute("club", club);
        YearMonth ym = YearMonth.now();
        model.addAttribute("mesVigencia", ym.getMonthValue());
        model.addAttribute("anioVigencia", ym.getYear());
        model.addAttribute("vigencias", categoriaCuotaVigenciaService.listarVigencias(categoria.getId()));

        return "categoria";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
                          BindingResult result,
                          @org.springframework.web.bind.annotation.RequestParam Integer mesVigencia,
                          @org.springframework.web.bind.annotation.RequestParam Integer anioVigencia,
                          Principal principal,
                          Model model,
                          HttpServletRequest request,
                          RedirectAttributes flash) {

        Usuario usuario = usuarioClubActivo(principal, request);
        if (usuario == null) {
            flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
            return "redirect:/seleccionarClub";
        }
        Club club = usuario.getClub();

        if (result.hasErrors()) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            model.addAttribute("club", club);
            model.addAttribute("mesVigencia", mesVigencia);
            model.addAttribute("anioVigencia", anioVigencia);
            model.addAttribute("vigencias", categoria.getId() != null
                    ? categoriaCuotaVigenciaService.listarVigencias(categoria.getId())
                    : List.<CategoriaValorVigencia>of());
            return "categoria";
        }
        
        Categoria existente = categoriaService.findByNombreAndClub(categoria.getNombre(), club);
        if (existente != null && (categoria.getId() == null || !existente.getId().equals(categoria.getId()))) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            model.addAttribute("club", club);
            model.addAttribute("nombreDuplicado", true);
            model.addAttribute("mesVigencia", mesVigencia);
            model.addAttribute("anioVigencia", anioVigencia);
            model.addAttribute("vigencias", categoria.getId() != null
                    ? categoriaCuotaVigenciaService.listarVigencias(categoria.getId())
                    : List.<CategoriaValorVigencia>of());
            return "categoria";
        }

        Categoria categoriaGuardar = categoria;
        if (categoria.getId() != null) {
            Categoria categoriaExistente = categoriaService.findById(categoria.getId());
            if (categoriaExistente == null || !categoriaExistente.getClub().getId().equals(club.getId())) {
                flash.addFlashAttribute("msjLayout", "error;Error;La categoría no existe o no pertenece al club.");
                return "redirect:/categorias/listar";
            }
            categoriaExistente.setNombre(categoria.getNombre());
            categoriaExistente.setClub(club);
            categoriaGuardar = categoriaExistente;
        } else {
            categoriaGuardar.setClub(club);
        }
        categoriaService.save(categoriaGuardar);
        try {
            categoriaCuotaVigenciaService.registrarVigencia(categoriaGuardar, anioVigencia, mesVigencia,
                    categoria.getValorCuota());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            model.addAttribute("club", club);
            model.addAttribute("mesVigencia", mesVigencia);
            model.addAttribute("anioVigencia", anioVigencia);
            model.addAttribute("vigenciaError", ex.getMessage());
            model.addAttribute("vigencias", categoriaGuardar.getId() != null
                    ? categoriaCuotaVigenciaService.listarVigencias(categoriaGuardar.getId())
                    : List.<CategoriaValorVigencia>of());
            model.addAttribute("categoria", categoriaGuardar);
            return "categoria";
        }
        
        flash.addFlashAttribute("msjLayout","success;Exito;Categoría guardada con éxito");
        return "redirect:/categorias/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Principal principal, HttpServletRequest request,
            RedirectAttributes flash) {
        Usuario usuario = usuarioClubActivo(principal, request);
        if (usuario == null) {
            flash.addFlashAttribute("msjLogin", "error;Club;Selecciona un club para continuar.");
            return "redirect:/seleccionarClub";
        }
        Club club = usuario.getClub();

        Categoria categoria = categoriaService.findById(id);

        if (categoria != null && categoria.getClub().getId().equals(club.getId())) {
            categoriaService.delete(id);
            flash.addFlashAttribute("msjLayout","success;Exito;Categoría eliminada correctamente");
        } else {
            flash.addFlashAttribute("msjLayout", "error;Error;No puede eliminar una categoría que no pertenece a su club");
        }

        return "redirect:/categorias/listar";
    }

    private Usuario usuarioClubActivo(Principal principal, HttpServletRequest request) {
        Long idClubSession = (Long) request.getSession().getAttribute("idClubSession");
        return usuarioService.resolveUsuarioActivo(principal.getName(), idClubSession);
    }
}
