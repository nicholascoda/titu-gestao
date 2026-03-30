package com.titu.core.controller;

import com.titu.core.dto.TituloDTO;
import com.titu.core.model.Titulo;
import com.titu.core.service.TituloService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/titulos")
@RequiredArgsConstructor
public class TituloController {

    private final TituloService service;

    @PostMapping
    public ResponseEntity<Void> criar(@RequestBody TituloDTO dados) {
        Titulo novoTitulo = new Titulo();
        novoTitulo.setValorOriginal(dados.valor());
        novoTitulo.setDataVencimento(dados.dataVencimento());
        novoTitulo.setDescricao(dados.descricao());
        novoTitulo.setStatus(dados.status());

        // Agora o Controller também fala em BigDecimal, alinhado com o Service!
        java.math.BigDecimal valorConvertido = dados.valor() != null ? dados.valor() : java.math.BigDecimal.ZERO;

        service.salvar(novoTitulo, dados.clienteId(), "UNICA", valorConvertido, 1);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}