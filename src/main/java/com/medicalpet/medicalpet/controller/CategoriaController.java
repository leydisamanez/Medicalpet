package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Categoria;
import com.medicalpet.medicalpet.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador para gestionar las categorías en la aplicación MedicalPet.
 */
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

  @Autowired
    private CategoriaService categoriaService;

  /**
     * Muestra la lista de todas las categorías.
     *
     * @param model Modelo para pasar las categorías a la vista
     * @return Nombre de la vista para listar categorías
     */
  @GetMapping("/listar")
    public String listar(Model model) {
    model.addAttribute("categorias", categoriaService.findAll());
    return "categorias/listar";
  }
  /**
     * Muestra el formulario para crear una nueva categoría.
     *
     * @param model Modelo para pasar un objeto categoría vacío a la vista
     * @return Nombre de la vista del formulario de nueva categoría
     */

  @GetMapping("/nueva")
    public String nueva(Model model) {
    model.addAttribute("categoria", new Categoria());
    return "categorias/nueva";
  }
  /**
     * Guarda una nueva categoría o actualiza una existente.
     *
     * @param categoria Objeto Categoria con los datos a guardar
     * @return Redirección a la lista de categorías
     */

  @PostMapping("/guardar")
    public String guardar(@ModelAttribute("categoria") Categoria categoria) {
    categoriaService.save(categoria);
    return "redirect:/categorias/listar";
  }
  /**
     * Muestra el formulario para editar una categoría existente.
     *
     * @param id ID de la categoría a editar
     * @param model Modelo para pasar la categoría a la vista
     * @return Nombre de la vista del formulario de edición
     * @throws IllegalArgumentException Si la categoría no se encuentra
     */

  @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
    model.addAttribute("categoria", categoriaService.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada")));
    return "categorias/form";
  }
  /**
     * Elimina una categoría según su ID.
     *
     * @param id ID de la categoría a eliminar
     * @return Redirección a la lista de categorías
     */

  @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
    categoriaService.deleteById(id);
    return "redirect:/categorias/listar";
  }
}