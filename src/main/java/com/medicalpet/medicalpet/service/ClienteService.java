package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Cliente;
import com.medicalpet.medicalpet.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> findByDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }

    public Page<Cliente> searchClientes(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return clienteRepository.findAll(pageable);
        }
        return clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrEmailContainingIgnoreCaseOrDniContaining(
                keyword, keyword, keyword, keyword, pageable);
    }
}