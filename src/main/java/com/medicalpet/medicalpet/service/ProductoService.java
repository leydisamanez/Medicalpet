package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Producto;
import com.medicalpet.medicalpet.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> findAll() {
        return productoRepository.findByActivoTrue();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }
}