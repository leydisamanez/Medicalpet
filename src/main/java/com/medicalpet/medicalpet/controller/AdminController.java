package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Usuario;
import com.medicalpet.medicalpet.repository.UsuarioRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * Controlador para la gestión de administradores en la aplicación MedicalPet.
 */

@Controller

@RequestMapping("/admin")
public class AdminController 
{
  @Autowired
    private UsuarioRepository usuarioRepository;
  @Autowired
    private PasswordEncoder passwordEncoder;

  /**
     * Lista todos los empleados registrados.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista para listar empleados
     */
  @GetMapping("/empleados")
    public String listarEmpleados(Model model) {
    List<Usuario> empleados = usuarioRepository.findAll();
    model.addAttribute("empleados", empleados);
    return "admin/empleados";
  }

  /**
     * Muestra el formulario para crear un nuevo empleado.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Nombre de la vista del formulario de registro
     */
  @GetMapping("/empleados/nuevo")
    public String nuevoEmpleadoForm(Model model) {
    model.addAttribute("usuario", new Usuario());
    return "registro";
  }
  /**
     * Crea un nuevo empleado con los datos proporcionados.
     *
     * @param usuario Objeto Usuario con los datos del empleado
     * @return Redirección a la lista de empleados
     */

  @PostMapping("/empleados")
    public String crearEmpleado(Usuario usuario) {
    usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
    usuario.setRol("ROLE_EMPLEADO");
    usuarioRepository.save(usuario);
    return "redirect:/admin/empleados";
  }
  /**
     * Elimina un empleado según su ID.
     *
     * @param id ID del empleado a eliminar
     * @return Redirección a la lista de empleados
     */

  @GetMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id) {
    usuarioRepository.deleteById(id);
    return "redirect:/admin/empleados";
  }
}