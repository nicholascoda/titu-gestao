package com.titu.core.controller;

import com.titu.core.model.Usuario;
import com.titu.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalAdvice {

    private final UsuarioRepository repository;

    // Esse método roda antes de qualquer tela do sistema ser aberta!
    @ModelAttribute("usuarioLogado")
    public Usuario adicionarUsuarioLogado(Principal principal) {
        if (principal != null) {
            // principal.getName() devolve o e-mail de quem logou
            return repository.findByEmail(principal.getName()).orElse(null);
        }
        return null;
    }
}