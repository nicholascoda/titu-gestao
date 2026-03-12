package com.titu.core.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "configuracoes_robo")
public class ConfiguracaoRobo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // As Chavinhas (Por padrão, já nascem ligadas = true)
    private boolean preventivoAtivo = true;
    private boolean vencimentoAtivo = true;
    private boolean atrasoAtivo = true;

    // Tom da Mensagem (Por padrão, nasce MEDIO)
    private String tomMensagem = "MEDIO"; // Opções que usaremos: FORMAL, MEDIO, INFORMAL
}
