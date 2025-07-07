package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Comprobante;
import com.medicalpet.medicalpet.model.DetalleVenta;
import com.medicalpet.medicalpet.model.Producto;
import com.medicalpet.medicalpet.model.Venta;
import com.medicalpet.medicalpet.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ComprobanteService comprobanteService;

    @Transactional
    public Venta saveVentaOnly(Venta venta) {
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        if (venta.getFecha() == null) {
            venta.setFecha(java.time.LocalDate.now());
        }
        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoService.save(producto);
            }
        }
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta save(Venta venta, Comprobante.TipoComprobante tipoComprobante, String ruc, double descuento) {
        if (venta.getCliente() == null) {
            throw new IllegalArgumentException("Cliente no puede ser nulo");
        }
        if (venta.getFecha() == null) {
            venta.setFecha(java.time.LocalDate.now());
        }

        // Save the Venta with all details and comprobante in one go
        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                if (producto.getStock() < detalle.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
                }
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoService.save(producto);
            }
        }

        Comprobante comprobante = venta.getComprobante();
        if (comprobante == null) {
            comprobante = new Comprobante();
            comprobante.setVenta(venta);
        }

        comprobante.setTipoComprobante(tipoComprobante);
        comprobante.setFecha(venta.getFecha());
        double base = venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        comprobante.setSubtotal(base);
        comprobante.setDescuento(descuento);
        if (tipoComprobante == Comprobante.TipoComprobante.FACTURA) {
            comprobante.setRuc(ruc != null ? ruc : "N/A");
            comprobante.setIgv(base * 0.18);
            comprobante.setTotal(base + comprobante.getIgv() - descuento);
        } else {
            comprobante.setIgv(0.0);
            comprobante.setTotal(base - descuento);
        }

        venta.setComprobante(comprobante);
        return ventaRepository.save(venta); // Single save to persist everything
    }

    public Optional<Venta> findById(Long id) {
        return ventaRepository.findById(id);
    }

    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    public List<Venta> findByProductoId(Long productoId) {
        return ventaRepository.findByDetallesProductoId(productoId);
    }

    @Transactional
    public void deleteById(Long id) {
        Venta venta = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada con ID: " + id));
        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoService.save(producto);
            }
        }
        if (venta.getComprobante() != null) {
            comprobanteService.deleteById(venta.getComprobante().getId());
        }
        ventaRepository.deleteById(id);
    }

    public Page<Venta> searchVentas(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return ventaRepository.findAll(pageable);
        }
        return ventaRepository.findByClienteNombreContainingIgnoreCaseOrFechaContaining(keyword, keyword, pageable);
    }
}