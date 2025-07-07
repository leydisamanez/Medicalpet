package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.model.Reporte;
import com.medicalpet.medicalpet.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * Controller class for managing report-related operations.
 */

@Controller
@RequestMapping("/reportes")
public class ReporteController {

  @Autowired
    private ReporteService reporteService;
  /**
     * Displays a list of all reports.
     *
     * @param model the model to add attributes
     * @return the view name for the report list
     */

  @GetMapping("/listar")
    public String listar(Model model) {
    model.addAttribute("reportes", reporteService.findAll());
    return "reportes/listar";
  }
  /**
     * Displays the form for creating a new report.
     *
     * @param model the model to add attributes
     * @return the view name for the report form
     */

  @GetMapping("/nuevo")
    public String nuevo(Model model) {
    model.addAttribute("reporte", new Reporte());
    return "reportes/form";
  }
  /**
     * Saves a new or updated report.
     *
     * @param reporte the report object to save
     * @return the redirect URL
     */

  @PostMapping("/guardar")
    public String guardar(@ModelAttribute("reporte") Reporte reporte) {
    reporteService.save(reporte);
    return "redirect:/reportes/listar";
  }
  /**
     * Displays the form for editing an existing report.
     *
     * @param id    the report ID
     * @param model the model to add attributes
     * @return the view name for the report form
     */

  @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
    model.addAttribute("reporte", reporteService.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado")));
    return "reportes/form";
  }
  /**
     * Deletes a report by ID.
     *
     * @param id the report ID
     * @return the redirect URL
     */
    
  @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
    reporteService.deleteById(id);
    return "redirect:/reportes/listar";
  }
  /**
     * Displays the details of a specific report.
     *
     * @param id    the report ID
     * @param model the model to add attributes
     * @return the view name for the report details
     */
    
  @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
    model.addAttribute("reporte", reporteService
        .findById(id).orElseThrow(() -> new IllegalArgumentException("Reporte no encontrado")));
    return "reportes/detalle";
  }
}