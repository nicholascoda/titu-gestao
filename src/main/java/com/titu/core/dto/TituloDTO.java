package com.titu.core.dto;

import com.titu.core.model.StatusTitulo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TituloDTO(
        BigDecimal valor,
        LocalDate dataVencimento,
        String descricao,
        Long clienteId,
        StatusTitulo status
) {}