package com.titu.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agendamentos_email")
public class AgendamentoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacionamento: Quem vai receber o e-mail?
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String assunto;

    // Usamos columnDefinition TEXT porque o corpo do e-mail pode ser bem grande
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    // A data e hora EXATA que você digitou lá no modal (Ex: 15/04/2026 às 08:00)
    @Column(nullable = false)
    private LocalDateTime dataHoraProgramada;

    // O Fuso Horário que você escolheu no select (Ex: "Asia/Shanghai" ou "America/Sao_Paulo")
    @Column(nullable = false)
    private String fusoHorarioDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAgendamento status = StatusAgendamento.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRecorrencia repeticao = TipoRecorrencia.UNICO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TomMensagem tomMensagem = TomMensagem.AMIGAVEL;

    // Campo de auditoria: Pra gente saber se o e-mail já foi disparado
    private LocalDateTime dataHoraEnvioReal;
}