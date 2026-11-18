package com.hijosdelasventas.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.service.BoletaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "API para procesar compras y revisar el historial de ventas")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @PostMapping
    @Operation(summary = "Procesar nueva compra", description = "Valida stock, descuenta, calcula el total real y genera la boleta con sus detalles")
    public ResponseEntity<?> crearCompra(@RequestBody Boleta boleta) {
        try {
            Boleta nuevaBoleta = boletaService.comprar(boleta);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaBoleta); // 201 Created
        } catch (IllegalArgumentException e) {
            // ARREGLO PUNTO 8 DEL BRANCO: Devolver JSON en vez de texto plano
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage())); 
        } catch (Exception e) {
            // Devolvemos el 500 también como JSON por si acaso
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error interno al procesar la compra")); 
        }
    }
    
    @GetMapping
    @Operation(summary = "Listar todas las boletas", description = "Retorna el historial completo de ventas")
    public ResponseEntity<List<Boleta>> listar() {
        return ResponseEntity.ok(boletaService.obtenerTodas()); // 200 OK
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de boleta por ID")
    public ResponseEntity<Boleta> obtener(@PathVariable Long id) {
        return boletaService.obtenerPorId(id)
                .map(ResponseEntity::ok) // 200 OK si existe
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 Not Found si no existe
    }
}