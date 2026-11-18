package com.hijosdelasventas.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hijosdelasventas.backend.model.Usuario;
import com.hijosdelasventas.backend.repository.UsuarioRepository;

@Service
public class UsuarioService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> obtenerTodos() { return usuarioRepository.findAll(); }
    public Optional<Usuario> obtenerPorId(Long id) { return usuarioRepository.findById(id); }

    public Usuario guardar(Usuario usuario, boolean esActualizacion) {
        if (esActualizacion) {
            Usuario existente = usuarioRepository.findById(usuario.getId()).orElseThrow();
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(existente.getPassword()); // Mantiene la contraseña si no se envía una nueva
            } else {
                usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }
        } else {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // Encripta al crear
        }
        return usuarioRepository.save(usuario);
    }

    public void eliminar(Long id) { usuarioRepository.deleteById(id); }
}