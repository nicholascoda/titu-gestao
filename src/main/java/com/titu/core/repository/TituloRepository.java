package com.titu.core.repository;

import com.titu.core.model.StatusTitulo;
import com.titu.core.model.Titulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TituloRepository extends JpaRepository<Titulo, Long> {

    // Para buscar tudo que vence hoje e ainda não foi pago (pro Robô usar depois)
    List<Titulo> findByDataVencimentoLessThanEqualAndStatus(LocalDate dataVencimento, StatusTitulo status);

    // Para listar as dívidas de um cliente específico
    List<Titulo> findByClienteId(Long clienteId);

    @Query("SELECT t FROM Titulo t JOIN FETCH t.cliente")
    List<Titulo> findAllComCliente();

    // 1. Soma tudo que é PENDENTE
    @Query("SELECT COALESCE(SUM(t.valorOriginal), 0) FROM Titulo t WHERE t.status = com.titu.core.model.StatusTitulo.PENDENTE")
    java.math.BigDecimal somarTotalPendente();

    // 2. Soma tudo que é PAGO
    @Query("SELECT COALESCE(SUM(t.valorOriginal), 0) FROM Titulo t WHERE t.status = com.titu.core.model.StatusTitulo.PAGO")
    java.math.BigDecimal somarTotalPago();

    // 3. Conta quantas cobranças VENCIDAS existem (Data < Hoje e Status = Pendente)
    @Query("SELECT COUNT(t) FROM Titulo t WHERE t.dataVencimento < CURRENT_DATE AND t.status = com.titu.core.model.StatusTitulo.PENDENTE")
    long contarVencidos();


    @Query("SELECT t FROM Titulo t JOIN FETCH t.cliente WHERE t.status = :status")
    List<Titulo> findAllByStatus(StatusTitulo status);

    // Busca só os VENCIDOS (Com cliente junto)
    @Query("SELECT t FROM Titulo t JOIN FETCH t.cliente WHERE t.dataVencimento < CURRENT_DATE AND t.status = com.titu.core.model.StatusTitulo.PENDENTE")
    List<Titulo> findVencidos();

    // Busca títulos de um cliente específico
    // ORDER BY t.status DESC -> Pendente aparece antes de Pago
    // t.dataVencimento DESC -> Os mais recentes/futuros aparecem primeiro
    @Query("SELECT t FROM Titulo t WHERE t.cliente.id = :clienteId ORDER BY t.status DESC, t.dataVencimento DESC")
    List<Titulo> buscarPorCliente(Long clienteId);

}