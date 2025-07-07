package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Cliente;
import com.medicalpet.medicalpet.service.ClienteService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for managing client-related operations.
 */

@Controller
@RequestMapping("/clientes")
public class ClienteController {
  @Autowired
    private ClienteService clienteService;
  /**
     * Displays a paginated list of clients with search and sorting capabilities.
     *
     * @param page    the page number (default 0)
     * @param size    the page size (default 10)
     * @param sort    the sorting field and order (default "nombre,asc")
     * @param keyword the search keyword (default empty)
     * @param model   the model to add attributes
     * @return the view name for the client list
     */

  @GetMapping("/listar")
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre,asc") String sort,
            @RequestParam(defaultValue = "") String keyword,
            Model model) {
    try {
      // Configurar paginación y ordenamiento
      String[] sortParams = sort.split(",");
      Sort sortOrder = Sort.by(sortParams[0]);
      if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")) {
                
        sortOrder = sortOrder.descending();
      } else {
        sortOrder = sortOrder.ascending();
      }
      Pageable pageable = PageRequest.of(page, size, sortOrder);

      // Obtener los clientes con búsqueda y paginación
      Page<Cliente> clientesPage = clienteService.searchClientes(keyword, pageable);

      // Agregar atributos al modelo
      model.addAttribute("clientes", clientesPage.getContent());
      model.addAttribute("currentPage", page);
      model.addAttribute("totalPages", clientesPage.getTotalPages());
      model.addAttribute("pageSize", size);
      model.addAttribute("sort", sort);
      model.addAttribute("keyword", keyword);
    
      model.addAttribute("sortId", "id," + (sort.startsWith("id") 
          ? (sort.endsWith("asc") ? "desc" : "asc") 
           : "asc"));
      model.addAttribute("sortNombre", "nombre," + (sort.startsWith("nombre") 
          ? (sort.endsWith("asc") ? "desc" : "asc") 
          : "asc"));
      model.addAttribute("sortApellido", "apellido," + (sort.startsWith("apellido") 
          ? (sort.endsWith("asc") ? "desc" : "asc") 
          : "asc"));
      model.addAttribute("sortEmail", "email," + (sort.startsWith("email") 
          ? (sort.endsWith("asc") ? "desc" : "asc") 
          : "asc"));
      model.addAttribute("sortDni", "dni," + (sort.startsWith("dni") 
          ? (sort.endsWith("asc") ? "desc" : "asc") 
          : "asc"));
            

    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar los clientes: " + e.getMessage());
      return "error";
    }
    return "clientes/listar";
  }
  /**
     * Displays the form for creating a new client.
     *
     * @param model the model to add attributes
     * @return the view name for the client form
     */

  @GetMapping("/nuevo")
    public String nuevoClienteForm(Model model) {
    try {
      model.addAttribute("cliente", new Cliente());
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar el formulario: " + e.getMessage());
      return "clientes/form";
    }
    return "clientes/form";
  }

  /**
     * Displays the form for editing an existing client.
     *
     * @param id    the client ID
     * @param model the model to add attributes
     * @return the view name for the client form
     */

  @GetMapping("/editar/{id}")
    public String editarClienteForm(@PathVariable Long id, Model model) {
    try {
      Cliente cliente = clienteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
      model.addAttribute("cliente", cliente);
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar los datos: " + e.getMessage());
      return "clientes/form";
    }
    return "clientes/form";
  }

  /**
     * Saves a client (new or updated).
     *
     * @param cliente the client object
     * @param model   the model to add attributes
     * @return the redirect URL
     */

  @PostMapping("/guardar")
    public String guardarCliente(@ModelAttribute("cliente") Cliente cliente, Model model) {
    try {
  
      clienteService.save(cliente); 
    } catch (Exception e) {
      model.addAttribute("error", "Error al guardar el cliente: " + e.getMessage());
      model.addAttribute("cliente", cliente);
      return "clientes/form";
    }
    return "redirect:/clientes/listar"; 
  }
  /**
     * Deletes a client by ID.
     *
     * @param id the client ID
     * @return the redirect URL
     */

  @GetMapping("/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id) {
     
    try {
      clienteService.deleteById(id);
    } catch (Exception e) {  
      return "redirect:/clientes/listar?error=" + e.getMessage();
    }
    return "redirect:/clientes/listar";
  }
  /**
     * Exports clients to a CSV file.
     *
     * @param response the HTTP response
     * @param keyword  the search keyword (default empty)
     * @throws IOException if an I/O error occurs
     */
    
  @GetMapping("/exportar")
    public void exportarClientes(HttpServletResponse response,
        @RequestParam(defaultValue = "") String keyword) throws IOException {
    try {
      response.setContentType("text/csv");
      response.setHeader("Content-Disposition", "attachment; filename=clientes.csv");

      Page<Cliente> clientesPage = clienteService.searchClientes(keyword, 
            PageRequest.of(0, Integer.MAX_VALUE)); 
      List<Cliente> clientes = clientesPage.getContent();

      StringBuilder csv = new StringBuilder();
      csv.append("ID,Nombre,Apellido,Email,Telefono,Direccion,DNI\n");
      for (Cliente cliente : clientes) {
        csv.append(cliente.getId()).append(",")
                   .append(cliente.getNombre() != null ? cliente.getNombre() : "N/A").append(",")
                  .append(cliente.getApellido() != null ? cliente.getApellido() : "N/A").append(",")
                   .append(cliente.getEmail() != null ? cliente.getEmail() : "N/A").append(",")
                  .append(cliente.getTelefono() != null ? cliente.getTelefono() : "N/A").append(",")
               .append(cliente.getDireccion() != null ? cliente.getDireccion() : "N/A").append(",")
                   .append(cliente.getDni() != null ? cliente.getDni() : "N/A").append("\n"); 
      }
      response.getWriter().write(csv.toString());
    } catch (Exception e) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Error al exportar: " + e.getMessage());
    }
  }
}