package com.hijosdelasventas.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.service.BoletaService;
import com.hijosdelasventas.backend.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping({"/api/v1/boletas", "/api/v1/ordenes"})
@Tag(name = "Boletas y Órdenes", description = "API para procesar compras y revisar el historial de ventas")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Procesar nueva compra", description = "Valida stock, descuenta, calcula el total real y genera la boleta con sus detalles")
    public ResponseEntity<Boleta> crearCompra(@RequestBody Boleta boleta, Authentication auth) {
        // El comprador sale del token (JwtFilter guarda el id como principal); sin token es invitado.
        // Ignoramos el usuario y la fecha que vengan en el cuerpo para que nadie compre a nombre de otro.
        boleta.setUsuario(auth != null && auth.getPrincipal() instanceof Long id
                ? usuarioService.obtenerPorId(id).orElse(null) : null);
        boleta.setFecha(null);
        Boleta nuevaBoleta = boletaService.comprar(boleta);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaBoleta);
    }

    @GetMapping
    @Operation(summary = "Listar todas las boletas", description = "Retorna el historial completo de ventas")
    public ResponseEntity<List<Boleta>> listar() {
        return ResponseEntity.ok(boletaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de boleta por ID")
    public ResponseEntity<Boleta> obtener(@PathVariable Long id) {
        return boletaService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}