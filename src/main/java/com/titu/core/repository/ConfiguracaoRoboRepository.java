package com.titu.core.repository;

import com.titu.core.model.ConfiguracaoRobo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoRoboRepository extends JpaRepository<ConfiguracaoRobo, Long> {
    // o Spring Boot cria aqui sozinho
}