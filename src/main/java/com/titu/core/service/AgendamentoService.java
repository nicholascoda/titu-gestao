package com.titu.core.service;

import com.titu.core.model.AgendamentoEmail;
import com.titu.core.model.StatusAgendamento;
import com.titu.core.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final EmailService emailService;
    private final LogAcaoService logService;

    // ==============================================================================
    // O ROBÔ DO CRM: Roda no segundo ZERO de todo santo minuto (A cada 60s)
    // ==============================================================================
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void verificarEDispararEmails() {

        // 1. Pega toda a galera que tá com status PENDENTE no banco
        List<AgendamentoEmail> pendentes = agendamentoRepository.findByStatus(StatusAgendamento.PENDENTE);

        for (AgendamentoEmail agendamento : pendentes) {
            try {
                // 2. A MÁGICA DO FUSO HORÁRIO
                // Pergunta pro servidor: "Que horas são AGORA lá no país que o cliente escolheu?"
                ZoneId fusoDestino = ZoneId.of(agendamento.getFusoHorarioDestino());
                LocalDateTime agoraNoDestino = ZonedDateTime.now(fusoDestino).toLocalDateTime();

// 3. Se a hora de lá já passou (ou é igual) a hora que agendamos, ATIRA!
                if (!agoraNoDestino.isBefore(agendamento.getDataHoraProgramada())) {

                    // Puxa o gatilho do e-mail
                    emailService.enviarEmailSimples(
                            agendamento.getCliente().getEmail(),
                            agendamento.getAssunto(),
                            agendamento.getTexto()
                    );

                    // Atualiza o ATUAL para ENVIADO (Blinda a Metralhadora!)
                    agendamento.setStatus(StatusAgendamento.ENVIADO);
                    agendamento.setDataHoraEnvioReal(LocalDateTime.now());
                    agendamentoRepository.save(agendamento);

                    // ==============================================================
                    // 4. A MÁGICA DA RECORRÊNCIA INFINITA
                    // Se não for "UNICO", a gente clona e joga pro futuro!
                    // ==============================================================
                    if (agendamento.getRepeticao() != com.titu.core.model.TipoRecorrencia.UNICO) {
                        AgendamentoEmail proximo = new AgendamentoEmail();
                        proximo.setCliente(agendamento.getCliente());
                        proximo.setAssunto(agendamento.getAssunto());
                        proximo.setTexto(agendamento.getTexto());
                        proximo.setFusoHorarioDestino(agendamento.getFusoHorarioDestino());
                        proximo.setTomMensagem(agendamento.getTomMensagem());
                        proximo.setRepeticao(agendamento.getRepeticao());
                        proximo.setStatus(StatusAgendamento.PENDENTE); // Nasce pendente para o mês que vem!

                        if (agendamento.getRepeticao() == com.titu.core.model.TipoRecorrencia.SEMANAL) {
                            proximo.setDataHoraProgramada(agendamento.getDataHoraProgramada().plusWeeks(1));
                        } else if (agendamento.getRepeticao() == com.titu.core.model.TipoRecorrencia.MENSAL) {
                            proximo.setDataHoraProgramada(agendamento.getDataHoraProgramada().plusMonths(1));
                        }

                        agendamentoRepository.save(proximo); // Coloca o clone na fila de espera
                    }

                    // Grava na fofoca do sistema
                    logService.registrarAcao("CRM_DISPARO", "Follow-up enviado para " +
                            agendamento.getCliente().getNomeEmpresa() + " (Fuso: " + agendamento.getFusoHorarioDestino() + ")");
                }

            } catch (Exception e) {
                // Se a internet cair ou o e-mail do cara for inválido, não deixa o robô travar!
                // Marca como FALHA e vai pro próximo.
                agendamento.setStatus(StatusAgendamento.FALHA);
                agendamentoRepository.save(agendamento);
                logService.registrarAcao("CRM_ERRO", "Falha ao enviar follow-up para " +
                        agendamento.getCliente().getNomeEmpresa() + ": " + e.getMessage());
            }
        }
    }

    // Mtodo para a tela salvar um novo agendamento (Vamos usar no próximo passo)
    public void agendarNovo(AgendamentoEmail novoAgendamento) {
        novoAgendamento.setStatus(StatusAgendamento.PENDENTE);
        agendamentoRepository.save(novoAgendamento);
        logService.registrarAcao("CRM_AGENDADO", "Novo e-mail programado para " + novoAgendamento.getCliente().getNomeEmpresa());
    }

    // Mtodo para a tela listar a tabela
    public List<AgendamentoEmail> listarTodos() {
        return agendamentoRepository.findAll();
    }
}