package com.medicalpet.medicalpet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Entity
public class Comprobante {
    public enum TipoComprobante {
        BOLETA, FACTURA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @Enumerated(EnumType.STRING)
    private TipoComprobante tipoComprobante;

    private int cantidad;
    private double precioUnitario;
    private double subtotal;
    private double igv;
    private double descuento;
    private double total;
    private LocalDate fecha;

    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener exactamente 11 dígitos")
    private String ruc;

    private String razon_social;
    private String direccion;

    // Constructors
    public Comprobante() {}

    public Comprobante(Venta venta, TipoComprobante tipoComprobante, int cantidad, double precioUnitario, double subtotal, double igv, double descuento, double total, LocalDate fecha, String ruc, String razonSocial, String direccion) {
        this.venta = venta;
        this.tipoComprobante = tipoComprobante;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.igv = igv;
        this.descuento = descuento;
        this.total = total;
        this.fecha = fecha;
        this.ruc = ruc;
        this.razon_social = razonSocial;
        this.direccion = direccion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getIgv() { return igv; }
    public void setIgv(double igv) { this.igv = igv; }
    public double getDescuento() { return descuento; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razon_social; }
    public void setRazonSocial(String razonSocial) { this.razon_social = razonSocial; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String toString() {
        return "Comprobante{id=" + id + ", tipoComprobante=" + tipoComprobante + ", venta=" + (venta != null ? venta.getId() : "N/A") +
               ", cantidad=" + cantidad + ", precioUnitario=" + precioUnitario +
               ", subtotal=" + subtotal + ", igv=" + igv + ", descuento=" + descuento +
               ", total=" + total + ", fecha=" + fecha + ", ruc='" + ruc + "', razonSocial='" + razon_social + "', direccion='" + direccion + "'}";
    }
}