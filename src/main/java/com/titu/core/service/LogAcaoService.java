package com.titu.core.service;

import com.titu.core.model.LogAcao;
import com.titu.core.repository.LogAcaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogAcaoService {

    private final LogAcaoRepository repository;

    // Esse é o metodo que vamos chamar de outros lugares do sistema
    public void registrarAcao(String acao, String descricao) {
        LogAcao log = LogAcao.builder()
                .dataHora(LocalDateTime.now())
                .usuario("Admin") // Quando tivermos a tela de login, a gente troca pelo nome real do usuário!
                .acao(acao)
                .descricao(descricao)
                .build();

        repository.save(log);
    }
}