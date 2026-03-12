package com.titu.core.repository;

import com.titu.core.model.ConfiguracaoRobo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoRoboRepository extends JpaRepository<ConfiguracaoRobo, Long> {
    // O Spring Boot faz a mágica aqui de graça
}