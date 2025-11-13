package com.app.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.app.entity.Club;
import com.app.entity.Usuario;
import com.app.service.ICategoriaService;
import com.app.service.IUsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("/listar")
    public String listar(Model model, Principal principal, HttpServletRequest request) {
        Usuario usuario = usuarioService.findByEmail(principal.getName());
        Club club = usuario.getClub();

        List<Categoria> listadoCategorias = categoriaService.findByClub(club);
        model.addAttribute("titulo", "Listado de Categorías del Club " + club.getNombre());
        model.addAttribute("listadoCategorias", listadoCategorias);

        return "listadoCategorias";
    }

    @GetMapping("/crear")
    public String crear(Model model, Principal principal) {
        Usuario usuario = usuarioService.findByEmail(principal.getName());
        Club club = usuario.getClub();

        Categoria categoria = new Categoria();
        categoria.setClub(club); // Se asocia automáticamente al club del usuario

        model.addAttribute("titulo", "Nueva Categoría");
        model.addAttribute("categoria", categoria);
        model.addAttribute("club", club);

        return "categoria";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, Principal principal, RedirectAttributes flash) {
        Usuario usuario = usuarioService.findByEmail(principal.getName());
        Club club = usuario.getClub();

        Categoria categoria = categoriaService.findById(id);
        if (categoria == null || !categoria.getClub().getId().equals(club.getId())) {
            flash.addFlashAttribute("error", "La categoría no existe o no pertenece a su club");
            return "redirect:/categorias/listar";
        }

        model.addAttribute("titulo", "Editar Categoría");
        model.addAttribute("categoria", categoria);
        model.addAttribute("club", club);

        return "categoria";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
                          BindingResult result,
                          Principal principal,
                          Model model,
                          RedirectAttributes flash) {

        Usuario usuario = usuarioService.findByEmail(principal.getName());
        Club club = usuario.getClub();

        if (result.hasErrors()) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            model.addAttribute("club", club);
            return "categoria";
        }
        
        Categoria existente = categoriaService.findByNombreAndClub(categoria.getNombre(), club);
        if (existente != null && (categoria.getId() == null || !existente.getId().equals(categoria.getId()))) {
            model.addAttribute("titulo", categoria.getId() == null ? "Nueva Categoría" : "Editar Categoría");
            model.addAttribute("club", club);
            model.addAttribute("nombreDuplicado", true);
            return "categoria";
        }

        categoria.setClub(club);
        categoriaService.save(categoria);
        
        flash.addFlashAttribute("msjLayout","success;Existo;Categoría guardada con éxito");
        return "redirect:/categorias/listar";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Principal principal, RedirectAttributes flash) {
        Usuario usuario = usuarioService.findByEmail(principal.getName());
        Club club = usuario.getClub();

        Categoria categoria = categoriaService.findById(id);

        if (categoria != null && categoria.getClub().getId().equals(club.getId())) {
            categoriaService.delete(id);
            flash.addFlashAttribute("msjLayout","success;Existo;Categoría eliminada correctamente");
        } else {
            flash.addFlashAttribute("msjLayout", "error;Error;No puede eliminar una categoría que no pertenece a su club");
        }

        return "redirect:/categorias/listar";
    }
}
