package com.titu.core.controller;

import com.titu.core.model.Usuario;
import com.titu.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository repository;
    // Trazemos a máquina de criptografar senhas!
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listarUsuarios(Model model) {
        // Manda a lista de tdo mundo pro HTML
        model.addAttribute("usuarios", repository.findAll());
        return "usuarios";
    }

    @PostMapping("/salvar")
    public String salvarUsuario(@ModelAttribute Usuario usuario) {
        // Pega a senha que o Gerente digitou na tela e embaralha ela!
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        repository.save(usuario);
        return "redirect:/usuarios";
    }

    @GetMapping("/excluir/{id}")
    public String excluirUsuario(@PathVariable Long id) {
        repository.deleteById(id);
        return "redirect:/usuarios";
    }
    @GetMapping("/perfil")
    public String meuPerfil() {
        // Como o GlobalAdvice já manda o "usuarioLogado" pra tela, não precisamos buscar nada no banco aqui!
        return "perfil";
    }
}