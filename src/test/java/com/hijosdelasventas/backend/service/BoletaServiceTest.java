package com.hijosdelasventas.backend.service;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hijosdelasventas.backend.model.Boleta;
import com.hijosdelasventas.backend.model.DetalleBoleta;
import com.hijosdelasventas.backend.model.Producto;
import com.hijosdelasventas.backend.repository.BoletaRepository;
import com.hijosdelasventas.backend.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
public class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private BoletaService boletaService;

    @Test
    void probarCompraExitosa() {
        // 1. Preparamos los datos
        Producto teclado = new Producto();
        teclado.setId(1L);
        teclado.setName("Teclado Mecánico");
        teclado.setStock(10);
        teclado.setPrice(50000);

        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setProducto(teclado);
        detalle.setQuantity(2); // Queremos comprar 2

        Boleta boleta = new Boleta();
        boleta.setItems(new ArrayList<>(java.util.List.of(detalle)));

        // Simulamos que la BD encuentra el teclado y guarda la boleta
        when(productoRepository.findById(1L)).thenReturn(Optional.of(teclado));
        when(boletaRepository.save(any(Boleta.class))).thenReturn(boleta);

        // 2. Ejecutamos el método
        Boleta resultado = boletaService.comprar(boleta);

        // 3. Verificamos que se descontó el stock y se calculó el total real
        assertNotNull(resultado);
        assertEquals(8, teclado.getStock()); // 10 - 2 = 8
        assertEquals(100000, boleta.getTotal()); // 50000 * 2 = 100000
        verify(productoRepository, times(1)).save(teclado);
        verify(boletaRepository, times(1)).save(boleta);
    }

    @Test
    void probarCompraStockInsuficiente() {
        // 1. Preparamos un producto con stock crítico
        Producto mouse = new Producto();
        mouse.setId(2L);
        mouse.setName("Mouse Gamer");
        mouse.setStock(1); // Queda 1 unidad

        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setProducto(mouse);
        detalle.setQuantity(5); // Intentamos comprar 5

        Boleta boleta = new Boleta();
        boleta.setItems(new ArrayList<>(java.util.List.of(detalle)));

        when(productoRepository.findById(2L)).thenReturn(Optional.of(mouse));

        // 2 y 3. Ejecutamos y verificamos que arroje la excepción correcta
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            boletaService.comprar(boleta);
        });

        assertEquals("Stock insuficiente para el producto: Mouse Gamer", excepcion.getMessage());
        verify(boletaRepository, never()).save(any(Boleta.class)); // Verificamos que NUNCA guarde la boleta
    }

    @Test
    void probarCompraConDescuentoCobraPrecioRebajado() {
        Producto perfume = new Producto();
        perfume.setId(1L);
        perfume.setName("Perfume");
        perfume.setStock(10);
        perfume.setPrice(50000);
        perfume.setDescuento(20); // 50000 -> 40000 por unidad

        DetalleBoleta detalle = new DetalleBoleta();
        detalle.setProducto(perfume);
        detalle.setQuantity(2);

        Boleta boleta = new Boleta();
        boleta.setItems(new ArrayList<>(java.util.List.of(detalle)));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(perfume));
        when(boletaRepository.save(any(Boleta.class))).thenReturn(boleta);

        boletaService.comprar(boleta);

        assertEquals(40000, detalle.getPrice());
        assertEquals(80000, boleta.getTotal());
    }

    @Test
    void probarBoletaSinItemsSeRechaza() {
        Boleta boleta = new Boleta();
        boleta.setItems(new ArrayList<>());

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> boletaService.comprar(boleta));

        assertEquals("La boleta no tiene productos", excepcion.getMessage());
        verify(boletaRepository, never()).save(any(Boleta.class));
    }
}
