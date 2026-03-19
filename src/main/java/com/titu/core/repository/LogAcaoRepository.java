package com.titu.core.repository;

import com.titu.core.model.LogAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAcaoRepository extends JpaRepository<LogAcao, Long> {

    // o Spring Boot lê esse nome e já faz o "ORDER BY data_hora DESC" sozinho
    List<LogAcao> findAllByOrderByDataHoraDesc();

}