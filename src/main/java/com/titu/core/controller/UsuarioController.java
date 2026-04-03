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
    //encriptografador de senhas do spring security
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listarUsuarios(Model model) {
        // Manda a lista de todos pro HTML
        model.addAttribute("usuarios", repository.findAll());

        // AQUI TÁ A CHAVE QUE LIGA A LUZ DO BOTÃO NO MENU!
        model.addAttribute("currentUri", "/usuarios");

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
    public String meuPerfil(Model model) {
        // Acende o botão de configurações no menu lateral
        model.addAttribute("currentUri", "/configuracoes");

        return "perfil";
    }
}