package com.titu.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "titulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Titulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O título deve ter um cliente")
    @ManyToOne(fetch = FetchType.LAZY) // Performance: Não carrega o cliente se não precisar
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser maior que zero")
    private BigDecimal valorOriginal;

    @NotNull(message = "Data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING) // Salva no banco como texto ("PENDENTE") e não número (0)
    @NotNull
    private StatusTitulo status;

    private String descricao;

    // Controle de versão para evitar conflito de edição
    @Version
    private Long version;
}