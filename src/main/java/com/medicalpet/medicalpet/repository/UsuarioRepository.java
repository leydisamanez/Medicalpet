package com.medicalpet.medicalpet.repository;

import com.medicalpet.medicalpet.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String username);
}