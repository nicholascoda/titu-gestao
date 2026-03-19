package com.titu.core.repository;

import com.titu.core.model.LogDisparo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LogDisparoRepository extends JpaRepository<LogDisparo, Long> {

    // traz o histórico de um cliente específico, ordenado do mais novo pro mais velho
    List<LogDisparo> findByClienteIdOrderByDataHoraEnvioDesc(Long clienteId);

    // traz apenas os 5 últimos disparos do robô, do mais novo pro mais velho
    List<com.titu.core.model.LogDisparo> findTop5ByOrderByDataHoraEnvioDesc();
}