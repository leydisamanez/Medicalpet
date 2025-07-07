
package com.medicalpet.medicalpet.service;

import com.medicalpet.medicalpet.model.Categoria;
import com.medicalpet.medicalpet.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    public void deleteById(Long id) {
        categoriaRepository.deleteById(id);
    }

    public List<Categoria> getAllCategorias() {
        return findAll();
    }
}