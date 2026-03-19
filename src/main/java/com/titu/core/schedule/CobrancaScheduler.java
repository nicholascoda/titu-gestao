package com.titu.core.schedule;

import com.titu.core.model.StatusTitulo;
import com.titu.core.model.Titulo;
import com.titu.core.repository.TituloRepository;
import com.titu.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // <--- IMPORTANTE

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CobrancaScheduler {

    private final TituloRepository tituloRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *") //
    @Transactional // mantém a conexão aberta com o banco
    public void verificarCobrancasDoDia() {
        LocalDate hoje = LocalDate.now();

        // busca quem venceu hoje OU antes
        List<Titulo> titulosVencendo = tituloRepository.findByDataVencimentoLessThanEqualAndStatus(hoje, StatusTitulo.PENDENTE);

        if(titulosVencendo.isEmpty()) {
            System.out.println("✅ Nenhuma cobrança pendente encontrada.");
            return;
        }

        System.out.println("🚨 Encontradas " + titulosVencendo.size() + " cobranças pendentes!");

        for (Titulo titulo : titulosVencendo) {
            // Agora o getCliente() vai funcionar porque estamos @Transactional
            String emailCliente = titulo.getCliente().getEmail();
            String valor = String.valueOf(titulo.getValorOriginal());

            // Lógica simples para o assunto
            String assunto = "Aviso de Cobrança Titu";

            String mensagem = "Olá " + titulo.getCliente().getNomeResponsavel() + ",\n\n" +
                    "Consta em nosso sistema uma pendência referente a '" + titulo.getDescricao() + "' " +
                    "no valor de R$ " + valor + " com vencimento em " + titulo.getDataVencimento() + ".\n\n" +
                    "Por favor, verifique.\n\n" +
                    "Atenciosamente,\nEquipe Titu Financeiro";

            emailService.enviarEmailSimples(emailCliente, assunto, mensagem);

            System.out.println("✉️ E-mail enviado para: " + emailCliente);
        }
    }
}