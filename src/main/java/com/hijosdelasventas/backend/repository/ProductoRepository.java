package com.hijosdelasventas.backend.repository;

import com.hijosdelasventas.backend.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoryId(Long categoriaId);
}