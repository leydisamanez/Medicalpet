package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Usuario;
import com.medicalpet.medicalpet.repository.UsuarioRepository;
import com.medicalpet.medicalpet.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    System.out.println("Buscando usuario: " + username);
    Usuario usuario = usuarioRepository.findByUsername(username);
    if (usuario == null) {
        System.out.println("Usuario no encontrado: " + username);
        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
    System.out.println("Usuario encontrado: " + usuario.getUsername() + ", Contraseña: " + usuario.getPassword() + ", Rol: " + usuario.getRol());
    return new CustomUserDetails(usuario);
}
}