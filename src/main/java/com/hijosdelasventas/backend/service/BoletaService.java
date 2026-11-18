package com.hijosdelasventas.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.model.DetalleBoleta;
import com.hijosdelasventas.backend.model.Producto;
import com.hijosdelasventas.backend.repository.BoletaRepository;
import com.hijosdelasventas.backend.repository.ProductoRepository;

@Service
public class BoletaService {

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public Boleta comprar(Boleta boleta) {
        int totalCalculado = 0;

        // 1. Recorrer los items para validar stock y blindar precios
        if (boleta.getItems() != null) {
            for (DetalleBoleta detalle : boleta.getItems()) {
                Producto productoDB = productoRepository.findById(detalle.getProducto().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + detalle.getProducto().getId()));

                // Validación de stock
                if (productoDB.getStock() < detalle.getQuantity()) {
                    throw new IllegalArgumentException("Stock insuficiente para el producto: " + productoDB.getName());
                }

                // Descuento de stock en la base de datos
                productoDB.setStock(productoDB.getStock() - detalle.getQuantity());
                productoRepository.save(productoDB);

                // ARREGLO PUNTO 7 DEL BRANCO: Agregamos el producto de la BD al detalle para que el frontend vea los nombres
                detalle.setProducto(productoDB);

                // SEGURIDAD: Forzamos el precio real desde la base de datos para evitar hackeos desde el frontend
                detalle.setPrice(productoDB.getPrice());

                // Sumar al total general de la boleta
                totalCalculado += (detalle.getPrice() * detalle.getQuantity());

                // Asociar la boleta al detalle para mantener la relación bidireccional de Hibernate
                detalle.setBoleta(boleta);
            }
        }

        // SEGURIDAD: Sobrescribimos el total que viene del frontend con nuestro total real calculado
        boleta.setTotal(totalCalculado);

        // 2. Guardar boleta (Hibernate guarda automáticamente los detalles por el CascadeType.ALL)
        return boletaRepository.save(boleta);
    }

    public List<Boleta> obtenerTodas() {
        return boletaRepository.findAll();
    }

    public Optional<Boleta> obtenerPorId(Long id) {
        return boletaRepository.findById(id);
    }
}