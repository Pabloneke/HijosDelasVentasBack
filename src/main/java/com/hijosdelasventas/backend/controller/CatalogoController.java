package com.hijosdelasventas.backend.controller;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.model.Producto;
import com.hijosdelasventas.backend.repository.BoletaRepository;
import com.hijosdelasventas.backend.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogoController {
    private final ProductoService productos;
    private final BoletaRepository boletas;

    public CatalogoController(ProductoService productos, BoletaRepository boletas) {
        this.productos = productos;
        this.boletas = boletas;
    }

    @GetMapping("/productos")
    public List<Producto> productos() {
        return productos.obtenerTodos();
    }

    @GetMapping("/productos/{id}")
    public ResponseEntity<Producto> producto(@PathVariable Long id) {
        return ResponseEntity.of(productos.obtenerPorId(id));
    }

    @GetMapping({"/ordenes", "/boletas"})
    public List<Boleta> ordenes() {
        return boletas.findAll();
    }

    @GetMapping({"/ordenes/{id}", "/boletas/{id}"})
    public ResponseEntity<Boleta> orden(@PathVariable Long id) {
        return ResponseEntity.of(boletas.findById(id));
    }
}
