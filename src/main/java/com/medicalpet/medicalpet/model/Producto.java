package com.medicalpet.medicalpet.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.time.LocalDate;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private double precio;
    private int stock;
    private String estado; // Asegúrate de que este campo esté presente
    private String ubicacion;
    private LocalDate vencimiento;
    private boolean activo = true;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    // Constructors
    public Producto() {}

    public Producto(String nombre, double precio, int stock, Categoria categoria, String estado, String ubicacion, LocalDate vencimiento, boolean activo) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.estado = estado;
        this.ubicacion = ubicacion;
        this.vencimiento = vencimiento;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getEstado() { return estado; } // Getter para estado
    public void setEstado(String estado) { this.estado = estado; } // Setter para estado
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public LocalDate getVencimiento() { return vencimiento; }
    public void setVencimiento(LocalDate vencimiento) { this.vencimiento = vencimiento; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Producto{id=" + id + ", nombre='" + nombre + "', precio=" + precio + 
               ", stock=" + stock + ", categoria=" + (categoria != null ? categoria.getId() : "N/A") + 
               ", estado='" + estado + "', ubicacion='" + ubicacion + "', vencimiento=" + vencimiento + 
               ", activo=" + activo + "}";
    }
}