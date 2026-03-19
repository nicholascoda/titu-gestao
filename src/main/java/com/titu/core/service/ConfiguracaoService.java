package com.titu.core.service;

import com.titu.core.model.ConfiguracaoRobo;
import com.titu.core.repository.ConfiguracaoRoboRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfiguracaoService {

    private final ConfiguracaoRoboRepository repository;

    public ConfiguracaoRobo obterConfiguracaoAtual() {
        List<ConfiguracaoRobo> configs = repository.findAll();

        // se o banco estiver vazio, cria o DEFAULT na hora
        if (configs.isEmpty()) {
            ConfiguracaoRobo padrao = new ConfiguracaoRobo();
            return repository.save(padrao);
        }

        return configs.get(0);
    }

    public void salvarConfiguracao(ConfiguracaoRobo novaConfig) {
        repository.save(novaConfig);
    }
}