package com.hijosdelasventas.backend;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hijosdelasventas.backend.model.Usuario;
import com.hijosdelasventas.backend.repository.UsuarioRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "auth.jwt.secret=AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8="
})
@AutoConfigureMockMvc
class BackendApplicationTests {
    private static final String AUTH = "/api/v1/auth";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder passwords;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @ParameterizedTest
    @CsvSource({
            "admin@duoc.cl, admin123, administrador",
            "vendedor@duoc.cl, vende123, vendedor",
            "carla@gmail.com, cliente123, cliente"
    })
    void usuariosInicialesPuedenIniciarSesion(String correo, String password, String tipo) throws Exception {
        mvc.perform(post(AUTH + "/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"correo":"%s","password":"%s"}
                """.formatted(correo, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.tipo").value(tipo));
    }

    @Test
    @Transactional
    void repetirCargaInicialCorrigeHashAntiguoYConservaCambios() throws Exception {
        Long idOcupado = jdbc.queryForObject("SELECT id FROM usuario WHERE correo = ?", Long.class, "admin@duoc.cl");
        jdbc.update("UPDATE usuario SET run = ?, correo = ?, tipo = ? WHERE id = ?",
                "123456785", "ana@example.com", "cliente", idOcupado);
        String hashPersonal = passwords.encode("clavePersonal123");
        jdbc.update("UPDATE usuario SET password = ? WHERE correo = ?", hashPersonal, "carla@gmail.com");
        jdbc.update("UPDATE usuario SET password = ? WHERE correo = ?",
                "$2a$10$8K1p/a0dL3.0R/2k4M8L0eR5h8O2k4M8L0eR5h8O2k4M8L0eR5h8O", "vendedor@duoc.cl");
        jdbc.update("UPDATE producto SET stock = ? WHERE id = ?", 7, 1);

        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(jdbc.getDataSource());

        Usuario admin = usuarios.findByCorreoIgnoreCase("admin@duoc.cl").orElseThrow();
        assertThat(admin.getId()).isNotEqualTo(idOcupado);
        assertThat(usuarios.findById(idOcupado).orElseThrow().getCorreo()).isEqualTo("ana@example.com");
        String hashVendedor = usuarios.findByCorreoIgnoreCase("vendedor@duoc.cl").orElseThrow().getPassword();
        assertThat(passwords.matches("vende123", hashVendedor)).isTrue();
        assertThat(usuarios.findByCorreoIgnoreCase("carla@gmail.com").orElseThrow().getPassword())
                .isEqualTo(hashPersonal);
        mvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6));
        mvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7));
    }

    @Test
    void registroLoginYRefresh() throws Exception {
        String registration = """
                {"run":"123456789","nombre":"Carla","apellidos":"Pérez","correo":"carla@example.com",
                 "password":"cliente123","tipo":"administrador"}
                """;
        mvc.perform(post(AUTH + "/registro").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.tipo").value("cliente"))
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
        Usuario usuario = usuarios.findByCorreoIgnoreCase("carla@example.com").orElseThrow();
        assertThat(usuario.getPassword()).isNotEqualTo("cliente123");
        assertThat(passwords.matches("cliente123", usuario.getPassword())).isTrue();