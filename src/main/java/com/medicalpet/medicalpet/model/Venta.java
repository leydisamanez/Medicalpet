package com.medicalpet.medicalpet.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate fecha;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @OneToOne(mappedBy = "venta", cascade = CascadeType.ALL)
    private Comprobante comprobante;

    @Version
    private Long version; // Added for optimistic locking

    // Constructors
    public Venta() {}

    public Venta(Cliente cliente, LocalDate fecha, List<DetalleVenta> detalles) {
        this.cliente = cliente;
        this.fecha = fecha;
        this.detalles = detalles;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public List<DetalleVenta> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVenta> detalles) { this.detalles = detalles; }

    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }

    public Long getVersion() { return version; } // Getter for version
    public void setVersion(Long version) { this.version = version; } // Setter for version

    @Override
    public String toString() {
        return "Venta{id=" + id + ", cliente=" + (cliente != null ? cliente.getId() : "N/A") +
               ", fecha=" + fecha + ", detalles=" + detalles + ", comprobante=" + (comprobante != null ? comprobante.getId() : "N/A") +
               ", version=" + version + "}";
    }
}