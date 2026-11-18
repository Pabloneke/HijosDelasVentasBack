package com.hijosdelasventas.backend;

import com.hijosdelasventas.backend.model.Usuario;
import com.hijosdelasventas.backend.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test",
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
        mvc.perform(post(AUTH + "/registro").contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isConflict());

        String login = """
                {"correo":"CARLA@example.com","password":"cliente123"}
                """;
        var session = mvc.perform(post(AUTH + "/login").contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().secure("refresh_token", true))
                .andReturn().getResponse();
        Cookie refresh = session.getCookie("refresh_token");
        String accessToken = json.readTree(session.getContentAsString()).get("accessToken").asText();
        mvc.perform(post(AUTH + "/refresh").cookie(refresh)).andExpect(status().isForbidden());
        mvc.perform(post(AUTH + "/refresh").header("X-Refresh-Token", "1")
                        .cookie(new Cookie("refresh_token", accessToken)))
                .andExpect(status().isUnauthorized());
        var renewed = mvc.perform(post(AUTH + "/refresh").header("X-Refresh-Token", "1").cookie(refresh))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        assertThat(json.readTree(renewed.getContentAsString()).get("accessToken").asText()).isNotEqualTo(accessToken);
        mvc.perform(get("/api/v1/productos").header(HttpHeaders.AUTHORIZATION, "Bearer " + refresh.getValue()))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(AUTH + "/login").contentType(MediaType.APPLICATION_JSON)
                        .content(login.replace("cliente123", "incorrecta")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void vendedorPuedeLeerPeroNoEscribir() throws Exception {
        usuarios.save(new Usuario(null, "987654321", "Juan", "Pérez", "vendedor@example.com",
                passwords.encode("vende123"), "vendedor", null, null, null));
        var session = mvc.perform(post(AUTH + "/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"correo":"vendedor@example.com","password":"vende123"}
                """)).andExpect(status().isOk()).andReturn().getResponse();
        String authorization = "Bearer " + json.readTree(session.getContentAsString()).get("accessToken").asText();
        for (String resource : new String[]{"/api/v1/productos", "/api/v1/ordenes", "/api/v1/boletas"}) {
            mvc.perform(get(resource)).andExpect(status().isUnauthorized());
            mvc.perform(get(resource).header(HttpHeaders.AUTHORIZATION, authorization)).andExpect(status().isOk());
            mvc.perform(post(resource).header(HttpHeaders.AUTHORIZATION, authorization)).andExpect(status().isForbidden());
            mvc.perform(put(resource + "/1").header(HttpHeaders.AUTHORIZATION, authorization)).andExpect(status().isForbidden());
            mvc.perform(patch(resource + "/1").header(HttpHeaders.AUTHORIZATION, authorization)).andExpect(status().isForbidden());
            mvc.perform(delete(resource + "/1").header(HttpHeaders.AUTHORIZATION, authorization)).andExpect(status().isForbidden());
        }
    }
}
