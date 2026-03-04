package com.titu.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;


    public void enviarEmailSimples(String para, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        mensagem.setFrom("robo@titusystem.com.br"); // Quem está enviando

        try {
            mailSender.send(mensagem);
            System.out.println("✅ E-mail enviado com sucesso para: " + para);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar e-mail: " + e.getMessage());
        }
    }



}