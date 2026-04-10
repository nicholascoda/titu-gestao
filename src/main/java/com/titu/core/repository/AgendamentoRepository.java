package com.titu.core.repository;

import com.titu.core.model.AgendamentoEmail;
import com.titu.core.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<AgendamentoEmail, Long> {

    // Busca no banco apenas os e-mails que ainda não foram enviados (PENDENTES)
    List<AgendamentoEmail> findByStatus(StatusAgendamento status);

}