package com.medicalpet.medicalpet.controller;

import com.medicalpet.medicalpet.service.ApisPeruService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller class for handling invoice-related API requests.
 */
@RestController
@RequestMapping("/comprobantes")
public class ComprobanteRestController {

  @Autowired
    private ApisPeruService apisPeruService;
  /**
     * Retrieves information for a given RUC (Registro Único de Contribuyentes).
     *
     * @param ruc the RUC number to consult
     * @return the response from the API service
     */

  @GetMapping("/consultar-ruc")
    
    public ResponseEntity<String> consultarRUC(@RequestParam String ruc) {
    try {
      String response = apisPeruService.consultarRUC(ruc);
      System.out.println("Enviando respuesta al frontend: " + response);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      String errorMsg = "{\"error\": \"Error al consultar RUC: " + e.getMessage() + "\"}";
      System.err.println(errorMsg);      return ResponseEntity.badRequest().body(errorMsg);
    }
  }
}