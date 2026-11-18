package com.hijosdelasventas.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "categoria")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera el constructor vacío Categoria()
@AllArgsConstructor // Genera el constructor con argumentos Categoria(Long id, String nombre)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(nullable = false, unique = true)
    private String nombre;
}
