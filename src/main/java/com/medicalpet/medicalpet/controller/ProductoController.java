package com.medicalpet.medicalpet.controller;




import com.medicalpet.medicalpet.model.Producto;
import com.medicalpet.medicalpet.service.CategoriaService;
import com.medicalpet.medicalpet.service.ProductoService;
import com.medicalpet.medicalpet.service.VentaService;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Controller class for managing product-related operations.
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {
  private static final Logger logger = LoggerFactory.getLogger(ProductoController.class);

  @Autowired
    private ProductoService productoService;
  @Autowired
    private CategoriaService categoriaService;
  @Autowired
    private VentaService ventaService;

  /**
     * Displays a list of all products.
     *
     * @param model the model to add attributes
     * @return the view name for the product list
     */

  @GetMapping("/listar")
    public String listarProductos(Model model) {
    try {
      List<Producto> productos = productoService.findAll();
      for (Producto p : productos) {
        logger.info("Producto cargado: {}", p);
      }
      model.addAttribute("productos", productos);
      model.addAttribute("ventaService", ventaService); // Para verificar ventas en la vista
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
      return "error";
    }
    return "productos/listar";
  }
  /**
     * Displays the form for creating a new product.
     *
     * @param model the model to add attributes
     * @return the view name for the product form
     */
    
  @GetMapping("/nuevo")
    public String nuevoProductoForm(Model model) {
    model.addAttribute("producto", new Producto());
    model.addAttribute("categorias", categoriaService.getAllCategorias());
    return "productos/form";
  }
  /**
     * Saves a new or updated product.
     *
     * @param producto the product object to save
     * @return the redirect URL
     */ 

  @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") Producto producto) {
    producto.setActivo(true); // Asegurar que el producto nuevo esté activo
    if (producto.getVencimiento() == null) {
      producto.setVencimiento(LocalDate.now().plusYears(1)); 
     
     

    }
    productoService.save(producto);
    return "redirect:/productos/listar";
  }
  /**
     * Displays the form for editing an existing product.
     *
     * @param id    the product ID
     * @param model the model to add attributes
     * @return the view name for the product form
     */

  @GetMapping("/editar/{id}")
    public String editarProductoForm(@PathVariable Long id, Model model) {
    model.addAttribute("producto", productoService.findById(id)
         .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado")));
    model.addAttribute("categorias", categoriaService.getAllCategorias());
    return "productos/form";
  }
  /**
     * Deletes a product if it has no associated sales.
     *
     * @param id         the product ID
     * @param uriBuilder the URI builder for redirects
     * @return the redirect URL
     */

  @GetMapping("/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
    try {
      if (!ventaService.findByProductoId(id).isEmpty()) {
        String errorMessage = "No se puede eliminar el producto porque tiene ventas asociadas.";
        return "redirect:" + uriBuilder.path("/productos/listar")
    .queryParam("error", errorMessage).build().toUriString();
      }
      productoService.deleteById(id);
    } catch (Exception e) {
      String errorMessage = "Error al eliminar el producto: " + e.getMessage();
      return "redirect:" + uriBuilder.path("/productos/listar")
    .queryParam("error", errorMessage).build().toUriString();
    }
    return "redirect:/productos/listar";
  }
  /**
     * Deactivates a product.
     *
     * @param id         the product ID
     * @param uriBuilder the URI builder for redirects
     * @return the redirect URL
     */

  @GetMapping("/desactivar/{id}")
    public String desactivarProducto(@PathVariable Long id, UriComponentsBuilder uriBuilder) {
    try {
      productoService.desactivar(id);
    } catch (Exception e) {
      String errorMessage = "Error al desactivar el producto: " + e.getMessage();
      return "redirect:" + uriBuilder.path("/productos/listar")
            .queryParam("error", errorMessage).build().toUriString();
    }
    return "redirect:/productos/listar";
  }
}