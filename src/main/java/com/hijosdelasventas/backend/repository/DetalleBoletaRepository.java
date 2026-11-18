package com.hijosdelasventas.backend.repository;

import com.hijosdelasventas.backend.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {
}