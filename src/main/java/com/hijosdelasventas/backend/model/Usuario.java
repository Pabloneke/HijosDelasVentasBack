package com.hijosdelasventas.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuario")
@Data // Genera automáticamente getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera el constructor vacío Usuario()
@AllArgsConstructor // Genera el constructor con todos los atributos
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El RUN es obligatorio")
    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Column(nullable = false)
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Column(nullable = false, unique = true)
    private String correo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "El tipo de rol es obligatorio")
    @Column(nullable = false)
    private String tipo; // administrador | vendedor | cliente

    private String direccion;
    private String region;
    private String comuna;
}
