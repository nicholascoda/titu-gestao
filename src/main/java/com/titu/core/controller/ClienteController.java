package com.titu.core.controller;

import com.titu.core.model.Cliente;
import com.titu.core.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes") // O endereço base será http://localhost:8080/clientes
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService service;

    @PostMapping
    public ResponseEntity<Cliente> criar(@RequestBody @Valid Cliente cliente) {
        System.out.println("🚨 CONTROLLER RODOU! Email: " + cliente.getEmail());

        Cliente novoCliente = service.salvar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
    }

    @GetMapping // Verbo HTTP para LER coisas
    public List<Cliente> listar() {
        return service.listarTodos();
    }
}