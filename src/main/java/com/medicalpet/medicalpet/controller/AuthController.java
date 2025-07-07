package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Usuario;
import com.medicalpet.medicalpet.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Controlador para manejar la autenticación y registro de usuarios en la aplicación MedicalPet.
 */
@Controller
public class AuthController {

  @Autowired
    private UsuarioService usuarioService;

  /**
     * Muestra la página de inicio de sesión.
     *
     * @return Nombre de la vista de login
     */
  @GetMapping("/login")
    public String login() {
    return "login";
  }
  /**
     * Muestra el formulario de registro de un nuevo usuario.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista de registro
     */

  @GetMapping("/registro")
    public String registro(Model model) {
    model.addAttribute("usuario", new Usuario());
    return "registro";
  }

  /**
     * Registra un nuevo usuario con los datos proporcionados.
     *
     * @param usuario Objeto Usuario con los datos del usuario a registrar
     * @param model Modelo para pasar mensajes de error a la vista
     * @return Redirección a la página de login si el registro es exitoso,
         */

  @PostMapping("/registro")
    public String registrarUsuario(@ModelAttribute("usuario") Usuario usuario, Model model) {
    try {
      // Validar si el usuario ya existe
      if (usuarioService.findByUsername(usuario.getUsername()) != null) {
        model.addAttribute("error", "El nombre de usuario ya está en uso.");
        return "registro";
      }
      usuarioService.save(usuario);
      return "redirect:/login?success=true";
    } catch (Exception e) {
      model.addAttribute("error", "Error al registrar el usuario: " + e.getMessage());
      return "registro";
    }
  }
}