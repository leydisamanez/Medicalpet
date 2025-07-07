package com.medicalpet.medicalpet.repository;

import com.medicalpet.medicalpet.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDni(String dni);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :apellido, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%')) " +
           "OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :dni, '%'))")
    Page<Cliente> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrDniContaining(
            String nombre, String apellido, String email, String dni, Pageable pageable);
}