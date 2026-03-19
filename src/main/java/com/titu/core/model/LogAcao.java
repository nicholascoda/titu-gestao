package com.titu.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "logs_acoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogAcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHora;

    private String usuario;

    private String acao; // Ex: "CRIAR", "PAGAR", "EXCLUIR", "EDITAR"

    private String descricao;
}