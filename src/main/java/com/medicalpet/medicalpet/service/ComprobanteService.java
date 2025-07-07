package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Comprobante;
import com.medicalpet.medicalpet.model.Venta;
import com.medicalpet.medicalpet.model.DetalleVenta;
import com.medicalpet.medicalpet.repository.ComprobanteRepository;
import com.medicalpet.medicalpet.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComprobanteService {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private VentaRepository ventaRepository;

    private static final double IGV_RATE = 0.18; // 18% IGV en Perú

    public List<Comprobante> findAll() {
        return comprobanteRepository.findAll();
    }

    public Optional<Comprobante> findById(Long id) {
        return comprobanteRepository.findById(id);
    }

    public Optional<Comprobante> findByVentaId(Long ventaId) {
        return comprobanteRepository.findByVentaId(ventaId);
    }

    public Comprobante save(Comprobante comprobante) {
        if (comprobante.getVenta() == null || comprobante.getVenta().getId() == null) {
            throw new IllegalArgumentException("Venta no puede ser nula o no persistida");
        }
        if (comprobante.getTipoComprobante() == Comprobante.TipoComprobante.FACTURA && (comprobante.getRuc() == null || !comprobante.getRuc().matches("\\d{11}"))) {
            throw new IllegalArgumentException("RUC inválido para factura");
        }
        if (comprobante.getFecha() == null) {
            comprobante.setFecha(java.time.LocalDate.now());
        }
        // Check for existing Comprobante for the same Venta
        Optional<Comprobante> existingComprobante = findByVentaId(comprobante.getVenta().getId());
        if (existingComprobante.isPresent() && !existingComprobante.get().getId().equals(comprobante.getId())) {
            throw new IllegalStateException("Ya existe un comprobante para la venta con ID: " + comprobante.getVenta().getId());
        }
        Venta venta = comprobante.getVenta();
        double base = venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        comprobante.setSubtotal(base);
        if (comprobante.getTipoComprobante() == Comprobante.TipoComprobante.BOLETA) {
            comprobante.setRuc(null);
            comprobante.setIgv(0.0);
            comprobante.setTotal(base - comprobante.getDescuento());
        } else {
            comprobante.setIgv(base * IGV_RATE);
            comprobante.setTotal(base + comprobante.getIgv() - comprobante.getDescuento());
        }
        return comprobanteRepository.save(comprobante);
    }

    public void deleteById(Long id) {
        comprobanteRepository.deleteById(id);
    }
}