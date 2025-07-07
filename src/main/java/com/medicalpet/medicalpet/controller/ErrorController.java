package com.medicalpet.medicalpet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * Controller class for handling error-related requests.
 */

@Controller
public class ErrorController {
  /**
     * Displays the access denied page.
     *
     * @return the view name for the access denied page
     */
  @GetMapping("/access-denied")
    public String accessDenied() { 
    return "access-denied";
  }
}