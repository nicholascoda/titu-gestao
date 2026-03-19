package com.titu.core.service;

import com.titu.core.model.LogAcao;
import com.titu.core.repository.LogAcaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogAcaoService {

    private final LogAcaoRepository repository;

    public void registrarAcao(String acao, String descricao) {

        // 1. Pega o NOME DO USUÁRIO LOGADO
        // Se não tiver ninguém logado fica um padrão "SISTEMA"
        String nomeUsuarioLogado = "SISTEMA";

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            nomeUsuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        // 2. Monta o Log com o Builder
        LogAcao log = LogAcao.builder()
                .dataHora(LocalDateTime.now())
                .usuario(nomeUsuarioLogado)
                .acao(acao)
                .descricao(descricao)
                .build();

        // 3. Salva no banco
        repository.save(log);
    }
}