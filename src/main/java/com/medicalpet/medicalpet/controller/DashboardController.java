package com.medicalpet.medicalpet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * Controller class for handling dashboard-related requests.
 */

@Controller
public class DashboardController {

  /**
     * Displays the dashboard view.
     *
     * @return the view name for the dashboard
     */
  @GetMapping("/dashboard")
    public String dashboard() {
    return "dashboard";
  }
}