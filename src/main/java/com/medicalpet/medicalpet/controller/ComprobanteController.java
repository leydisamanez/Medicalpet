package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Comprobante;
import com.medicalpet.medicalpet.service.ComprobanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller class for managing invoice-related operations.
 */
@Controller
@RequestMapping("/comprobantes")
public class ComprobanteController {

  @Autowired
    private ComprobanteService comprobanteService;

  @Autowired
  
  // private VentaService ventaService;

  private static final String EMPRESA_NOMBRE = "MedicalPet S.A.C.";
  private static final String EMPRESA_RUC = "20512345678";
  private static final String EMPRESA_DIRECCION = "Av. Veterinaria 123, Lima, Perú"; 
  private static final String EMPRESA_TELEFONO = "+51 987 654 321";

  /**
     * Displays a list of all invoices.
     *
     * @param model the model to add attributes
     * @return the view name for the invoice list
     */

  @GetMapping("/listar")
    public String listar(Model model) {
    try {
      model.addAttribute("comprobantes", comprobanteService.findAll());
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar los comprobantes: " + e.getMessage());
      return "error";
    }
    return "comprobantes/listar";
  }
  /**
     * Displays the details of a specific invoice.
     *
     * @param id    the invoice ID
     * @param model the model to add attributes
     * @return the view name for the invoice details
     */

  @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
    try {
        
      Comprobante comprobante = comprobanteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
      model.addAttribute("comprobante", comprobante);
      model.addAttribute("empresaNombre", EMPRESA_NOMBRE);
      model.addAttribute("empresaRuc", EMPRESA_RUC); 
      model.addAttribute("empresaDireccion", EMPRESA_DIRECCION);
      model.addAttribute("empresaTelefono", EMPRESA_TELEFONO);
    } catch (Exception e) {
      model.addAttribute("error", "Error al cargar el comprobante: " + e.getMessage());
      return "error";
    }
    return "comprobantes/detalle";
  }

  /**
     * Exports a specific invoice as a PDF.
     *
     * @param id    the invoice ID
     * @param model the model to add attributes
     * @return the view name for the PDF export
     */

  @GetMapping("/exportar-pdf/{id}")
    public String 
         exportarPDF(@PathVariable Long id, Model model) {
    try {
      Comprobante comprobante = comprobanteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Comprobante no encontrado"));
      model.addAttribute("comprobante", comprobante);
      model.addAttribute("empresaNombre", EMPRESA_NOMBRE);
      model.addAttribute("empresaRuc", EMPRESA_RUC);
      model.addAttribute("empresaDireccion", EMPRESA_DIRECCION);
      model.addAttribute("empresaTelefono", EMPRESA_TELEFONO);
    } catch (Exception e) {
      model.addAttribute("error", "Error al generar el PDF: " + e.getMessage());
      return "error";
    }
    return "comprobantes/pdf";
  }
}