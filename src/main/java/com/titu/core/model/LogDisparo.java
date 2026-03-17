package com.titu.core.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logs_disparos")
public class LogDisparo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Conectando o log ao cliente que recebeu a cobrança
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    private String tipoRegua; // Ex: PREVENTIVO, HOJE, ATRASO
    private String assunto;

    @Column(columnDefinition = "TEXT") // TEXT permite salvar a mensagem inteira sem cortar
    private String mensagem;

    private LocalDateTime dataHoraEnvio;
}