package com.titu.core.repository;

import com.titu.core.model.LogAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogAcaoRepository extends JpaRepository<LogAcao, Long> {

    // O Spring Boot é mágico, ele lê esse nome em inglês e já faz o "ORDER BY data_hora DESC" sozinho no SQL!
    List<LogAcao> findAllByOrderByDataHoraDesc();

}