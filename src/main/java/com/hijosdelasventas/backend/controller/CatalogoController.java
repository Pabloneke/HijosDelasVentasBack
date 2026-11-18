package com.hijosdelasventas.backend.controller;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.repository.BoletaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogoController {
    private final BoletaRepository boletas;

    public CatalogoController(BoletaRepository boletas) {
        this.boletas = boletas;
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
