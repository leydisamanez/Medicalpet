package com.medicalpet.medicalpet.repository;

import com.medicalpet.medicalpet.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    @Query("SELECT v FROM Venta v JOIN v.detalles d JOIN d.producto p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.cliente.nombre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(v.fecha AS string) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Venta> findByClienteNombreContainingIgnoreCaseOrFechaContaining(String nombre, String fecha, Pageable pageable);

    @Query("SELECT v FROM Venta v JOIN v.detalles d WHERE d.producto.id = :productoId")
    List<Venta> findByDetallesProductoId(@Param("productoId") Long productoId);
}