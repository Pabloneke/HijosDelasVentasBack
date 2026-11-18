package com.hijosdelasventas.backend.controller;

import com.hijosdelasventas.backend.model.Usuario;
import com.hijosdelasventas.backend.repository.UsuarioRepository;
import com.hijosdelasventas.backend.service.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final String REFRESH_COOKIE = "refresh_token";
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtService jwtService;
    private final boolean secureCookie;
    private final String dummyHash;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder passwords, JwtService jwtService,
                          @Value("${auth.refresh.secure-cookie}") boolean secureCookie) {
        this.usuarios = usuarios;
        this.passwords = passwords;
        this.jwtService = jwtService;
        this.secureCookie = secureCookie;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    @PostMapping("/registro")
    public ResponseEntity<SessionResponse> registro(@Valid @RequestBody Registro request) {
        validatePasswordLength(request.password());
        String run = request.run().trim();
        String correo = request.correo().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByRun(run) || usuarios.existsByCorreoIgnoreCase(correo)) {
            throw duplicateUser();
        }
        Usuario usuario = new Usuario(null, run, request.nombre().trim(), request.apellidos().trim(),
                correo, passwords.encode(request.password()), "cliente",
                request.direccion(), request.region(), request.comuna());
        try {
            return session(usuarios.saveAndFlush(usuario), HttpStatus.CREATED, true);
        } catch (DataIntegrityViolationException exception) {
            if (usuarios.existsByRun(run) || usuarios.existsByCorreoIgnoreCase(correo)) {
                throw duplicateUser();
            }
            throw exception;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<SessionResponse> login(@Valid @RequestBody Login request) {
        validatePasswordLength(request.password());
        Usuario usuario = usuarios.findByCorreoIgnoreCase(request.correo().trim()).orElse(null);
        boolean valid = passwords.matches(request.password(), usuario == null ? dummyHash : usuario.getPassword());
        if (!valid || usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }
        return session(usuario, HttpStatus.OK, true);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SessionResponse> refresh(
            @RequestHeader(value = "X-Refresh-Token", defaultValue = "") String refreshHeader,
            @CookieValue(value = REFRESH_COOKIE, defaultValue = "") String refreshToken) {
        if (!"1".equals(refreshHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Usuario usuario = jwtService.validateRefresh(refreshToken).flatMap(usuarios::findById)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión vencida o inválida"));
        return session(usuario, HttpStatus.OK, false);
    }

    private ResponseEntity<SessionResponse> session(Usuario usuario, HttpStatus status, boolean issueRefresh) {
        var response = ResponseEntity.status(status).header(HttpHeaders.CACHE_CONTROL, "no-store");
        if (issueRefresh) {
            ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, jwtService.refreshToken(usuario.getId()))
                    .httpOnly(true)
                    .secure(secureCookie)
                    .sameSite("Strict")
                    .path("/api/v1/auth/refresh")
                    .maxAge(JwtService.REFRESH_DURATION)
                    .build();
            response.header(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        var profile = new Profile(usuario.getId(), usuario.getNombre(), usuario.getApellidos(),
                usuario.getCorreo(), usuario.getTipo());
        return response.body(new SessionResponse(jwtService.accessToken(usuario.getId()),
                JwtService.ACCESS_DURATION.toSeconds(), profile));
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña excede 72 bytes");
        }
    }

    private ResponseStatusException duplicateUser() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "El correo o RUN ya está registrado");
    }

    public record Login(@NotBlank @Email @Size(max = 255) String correo, @NotBlank String password) {
        @Override
        public String toString() {
            return "Login";
        }
    }

    public record Registro(@NotBlank @Size(max = 12) String run,
                           @NotBlank @Size(max = 255) String nombre,
                           @NotBlank @Size(max = 255) String apellidos,
                           @NotBlank @Email @Size(max = 255) String correo,
                           @NotBlank @Size(min = 8) String password,
                           @Size(max = 255) String direccion,
                           @Size(max = 255) String region,
                           @Size(max = 255) String comuna) {
        @Override
        public String toString() {
            return "Registro";
        }
    }

    public record Profile(Long id, String nombre, String apellidos, String correo, String tipo) {}

    public record SessionResponse(String accessToken, long expiresIn, Profile usuario) {
        @Override
        public String toString() {
            return "SessionResponse";
        }
    }
}
