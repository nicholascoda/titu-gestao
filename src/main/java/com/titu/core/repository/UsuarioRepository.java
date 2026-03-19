package com.titu.core.repository;

import com.titu.core.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // O Security usa esse metodo para achar quem está tentando logar
    Optional<Usuario> findByEmail(String email);

}