package com.titu.core.scheduler;

import com.titu.core.model.Titulo;
import com.titu.core.model.StatusTitulo;
import com.titu.core.model.ConfiguracaoRobo;
import com.titu.core.repository.TituloRepository;
import com.titu.core.service.EmailService;
import com.titu.core.service.ConfiguracaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor // <-- cria o construtor automaticamente para injetarmos o repositório
public class RoboDeCobranca {

    private final TituloRepository tituloRepository;
    private final EmailService emailService;
    private final ConfiguracaoService configuracaoService; // <-- Lê as chavinhas do banco
    private final com.titu.core.repository.LogDisparoRepository logDisparoRepository; // <-- O Caderninho de anotações!

    // a cada 1 minuto ("0 * * * * *") APENAS PARA TESTES!
    @Transactional
    @Scheduled(cron = "0 * * * * *")
    public void executarRotinaDiaria() {

        String horaAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalDate hoje = LocalDate.now();

        // Puxa as configurações atuais da tela de Automações
        ConfiguracaoRobo config = configuracaoService.obterConfiguracaoAtual();

        System.out.println("==================================================");
        System.out.println("🤖 [TITU-BÔ] BEEP BOOP! Acordei às: " + horaAtual);
        System.out.println("🎛️ [PAINEL] Tom de voz configurado: " + config.getTomMensagem());

        // ---------------------------------------------------------
        // 1. RÉGUA PREVENTIVA (3 DIAS ANTES)
        // ---------------------------------------------------------
        if (config.isPreventivoAtivo()) {
            LocalDate daquiATresDias = hoje.plusDays(3);
            System.out.println("🔍 [ROBÔ] A procurar faturas PREVENTIVAS que vencem no dia: " + daquiATresDias.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            List<Titulo> preventivos = tituloRepository.findByStatusAndDataVencimento(StatusTitulo.PENDENTE, daquiATresDias);
            processarCobrancas(preventivos, "preventiva", config.getTomMensagem(), "PREVENTIVO");
        } else {
            System.out.println("⏸️ [ROBÔ] Régua Preventiva está DESLIGADA nas configurações.");
        }

        // ---------------------------------------------------------
        // 2. RÉGUA DE VENCIMENTO (HOJE)
        // ---------------------------------------------------------
        if (config.isVencimentoAtivo()) {
            System.out.println("🔍 [ROBÔ] A procurar faturas que vencem HOJE: " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            List<Titulo> vencemHoje = tituloRepository.findByStatusAndDataVencimento(StatusTitulo.PENDENTE, hoje);
            processarCobrancas(vencemHoje, "para hoje", config.getTomMensagem(), "HOJE");
        } else {
            System.out.println("⏸️ [ROBÔ] Régua de Vencimento (Hoje) está DESLIGADA nas configurações.");
        }

        // ---------------------------------------------------------
        // 3. RÉGUA DE ATRASO (ONTEM)
        // ---------------------------------------------------------
        if (config.isAtrasoAtivo()) {
            LocalDate ontem = hoje.minusDays(1);
            System.out.println("🔍 [ROBÔ] A procurar faturas ATRASADAS do dia: " + ontem.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            List<Titulo> atrasados = tituloRepository.findByStatusAndDataVencimento(StatusTitulo.PENDENTE, ontem);
            processarCobrancas(atrasados, "em atraso", config.getTomMensagem(), "ATRASO");
        } else {
            System.out.println("⏸️ [ROBÔ] Régua de Atraso está DESLIGADA nas configurações.");
        }

        System.out.println("💤 [ROBÔ] Varredura concluída. A voltar para a base.");
        System.out.println("==================================================\n");
    }

    // --- MeTODO AUXILIAR PARA MANTER O SEU PADRÃO DE AVISOS ---
    private void processarCobrancas(List<Titulo> titulos, String nomeRegua, String tom, String tipoRegua) {

        // Exatamente a sua lógica de avisos
        if (titulos.isEmpty()) {
            System.out.println("✅ [ROBÔ] Nenhuma cobrança " + nomeRegua + ". Que paz!");
        } else {
            System.out.println("⚠️ [ROBÔ] Encontrei " + titulos.size() + " cliente(s) para notificar!");

            for (Titulo titulo : titulos) {
                // 1. O Robô avisa quem ele colocou na mira primeiro
                System.out.println("   -> ⏳ A preparar e-mail para: " + titulo.getCliente().getNomeEmpresa() + " | Valor: R$ " + titulo.getValorOriginal());

                // 2. Pega os textos corretos do dicionário lá embaixo
                String[] textos = obterMensagemPorTom(tom, tipoRegua);
                String assunto = textos[0];
                String textoPrincipal = textos[1];

                // 3. Monta a munição (mensagem) juntando o texto do dicionário com os dados reais
                String mensagem = "Olá equipe da " + titulo.getCliente().getNomeEmpresa() + ",\n\n" +
                        textoPrincipal + "\n\n" +
                        "Valor: R$ " + titulo.getValorOriginal() + "\n" +
                        "Vencimento: " + titulo.getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n\n" +
                        "Caso já tenha efetuado o pagamento, por favor, desconsidere esta mensagem.\n\n" +
                        "Um abraço,\nEquipe Titu Gestão";

                // 4. Puxa o gatilho!
                emailService.enviarEmailSimples(titulo.getCliente().getEmail(), assunto, mensagem);

                // --- A CAIXA PRETA: Registrando o tiro no banco de dados! ---
                com.titu.core.model.LogDisparo log = new com.titu.core.model.LogDisparo();
                log.setCliente(titulo.getCliente());
                log.setTipoRegua(tipoRegua);
                log.setAssunto(assunto);
                log.setMensagem(mensagem);
                log.setDataHoraEnvio(LocalDateTime.now());
                logDisparoRepository.save(log); // Salva no banco!

                System.out.println("   -> 📝 Log de disparo salvo com sucesso para " + titulo.getCliente().getNomeEmpresa());

                // 5. O Robô "respira" por 2 segundos para o Mailtrap não bloquear
                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // --- O DICIONÁRIO DE PACKS DE MENSAGENS 📖 ---
    // Retorna um Array com 2 posições: [0] = Assunto, [1] = Texto Principal
    private String[] obterMensagemPorTom(String tom, String tipoRegua) {

        if (tom.equals("FORMAL")) {
            switch (tipoRegua) {
                case "PREVENTIVO": return new String[]{"Aviso Prévio de Vencimento", "Prezados, servimo-nos do presente para informar que o título especificado abaixo vencerá em 3 dias."};
                case "HOJE": return new String[]{"Notificação de Vencimento no Dia Corrente", "Prezados, notificamos que o prazo para pagamento da fatura especificada abaixo expira na data de hoje."};
                case "ATRASO": return new String[]{"Notificação Extrajudicial: Fatura em Atraso", "Prezados, verificamos em nossos registros que não consta o pagamento da fatura vencida ontem. Solicitamos a regularização."};
            }
        }

        else if (tom.equals("INFORMAL")) {
            switch (tipoRegua) {
                case "PREVENTIVO": return new String[]{"E aí! Só lembrando da fatura que vence logo mais", "Tudo bem por aí? Passando rapidinho só para avisar que a fatura abaixo vence daqui a 3 dias. Qualquer dúvida, dá um grito!"};
                case "HOJE": return new String[]{"Hoje é o dia da sua fatura! 🚀", "Opa! Passando para lembrar que hoje é o dia do vencimento da sua fatura. Dá uma olhada nos dados abaixo:"};
                case "ATRASO": return new String[]{"Ih, acho que você esqueceu! 😅", "Ei, tudo bem? Notamos que o pagamento da fatura vencida ontem ainda não caiu aqui pra gente. Dá uma conferida se rolou algum problema!"};
            }
        }

        // PADRÃO (MEDIO/COMERCIAL)
        else {
            switch (tipoRegua) {
                case "PREVENTIVO": return new String[]{"Lembrete: Sua fatura vence em 3 dias", "Este é um lembrete automático. Sua fatura vencerá em breve, conforme os detalhes abaixo:"};
                case "HOJE": return new String[]{"Aviso: Sua fatura vence HOJE", "Atenção! Sua fatura vence no dia de hoje. Garanta o pagamento para evitar possíveis juros."};
                case "ATRASO": return new String[]{"URGENTE: Fatura Atrasada", "Identificamos que a sua fatura encontra-se em aberto em nosso sistema. Por favor, regularize a situação o quanto antes."};
            }
        }

        return new String[]{"Aviso", "Aviso do sistema."}; // Proteção anti-bug
    }
}