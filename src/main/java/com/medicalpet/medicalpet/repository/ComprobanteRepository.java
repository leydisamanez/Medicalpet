package com.medicalpet.medicalpet.repository;

import com.medicalpet.medicalpet.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByVentaId(Long ventaId);
}