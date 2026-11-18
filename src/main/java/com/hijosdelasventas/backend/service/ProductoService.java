package com.hijosdelasventas.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hijosdelasventas.backend.model.Categoria;
import com.hijosdelasventas.backend.model.Producto;
import com.hijosdelasventas.backend.repository.CategoriaRepository;
import com.hijosdelasventas.backend.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardar(Producto producto) {
        if (producto.getCategory() == null || producto.getCategory().getId() == null) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria");
        }
        Categoria categoriaDB = categoriaRepository.findById(producto.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + producto.getCategory().getId()));
        
        producto.setCategory(categoriaDB);
        return productoRepository.save(producto);
    }

    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }
}