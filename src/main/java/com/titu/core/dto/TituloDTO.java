package com.titu.core.dto;

import com.titu.core.model.StatusTitulo;

import java.math.BigDecimal;
import java.time.LocalDate;

// Record: É um jeito novo do Java de criar classes só de dados (sem getters/setters manuais)
public record TituloDTO(
        BigDecimal valor,
        LocalDate dataVencimento,
        String descricao,
        Long clienteId, // O ID de quem vai pagar
        StatusTitulo status // Novo campo (opcional)
) {}