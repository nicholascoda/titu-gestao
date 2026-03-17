package com.titu.core.config;

import com.titu.core.model.Cliente;
import com.titu.core.model.LogDisparo;
import com.titu.core.model.StatusTitulo;
import com.titu.core.model.Titulo;
import com.titu.core.repository.ClienteRepository;
import com.titu.core.repository.LogDisparoRepository;
import com.titu.core.repository.TituloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class MockDataConfig implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final TituloRepository tituloRepository;
    private final LogDisparoRepository logDisparoRepository;

    @Override
    public void run(String... args) throws Exception {

        // Só injeta os dados se a tabela de clientes estiver vazia!
        if (clienteRepository.count() == 0) {
            System.out.println("🌱 [DEV] Populando banco de dados com dados fictícios para demonstração...");

            // 1. Criando 3 Clientes Fictícios
            Cliente c1 = Cliente.builder().nomeEmpresa("Tech Solutions TI").nomeResponsavel("Carlos Mendes").email("carlos@techsolutions.com").telefone("(11) 98888-7777").cnpj("12.345.678/0001-90").usarRegrasGlobais(true).build();
            Cliente c2 = Cliente.builder().nomeEmpresa("Agência Vanguarda").nomeResponsavel("Mariana Luz").email("financeiro@vanguarda.cx").telefone("(21) 97777-6666").cnpj("98.765.432/0001-10").usarRegrasGlobais(false).tomMensagem("INFORMAL").preventivoAtivo(true).vencimentoAtivo(true).atrasoAtivo(false).build();
            Cliente c3 = Cliente.builder().nomeEmpresa("Padaria Pão de Ouro").nomeResponsavel("Seu Zé").email("contato@paodeouro.com").telefone("(31) 99999-5555").cnpj("45.678.123/0001-45").usarRegrasGlobais(true).build();

            clienteRepository.saveAll(Arrays.asList(c1, c2, c3));

            // 2. Criando Títulos (Pagos, Pendentes e Atrasados) para gerar o Gráfico!
            // Pagos mês atual
            Titulo t1 = Titulo.builder().cliente(c1).descricao("Manutenção Servidor").valorOriginal(new BigDecimal("1500.00")).dataVencimento(LocalDate.now().minusDays(10)).status(StatusTitulo.PAGO).dataPagamento(LocalDate.now().minusDays(9)).build();
            Titulo t2 = Titulo.builder().cliente(c2).descricao("Campanha Marketing").valorOriginal(new BigDecimal("3200.00")).dataVencimento(LocalDate.now().minusDays(5)).status(StatusTitulo.PAGO).dataPagamento(LocalDate.now().minusDays(5)).build();

            // Pendentes mês atual (A receber)
            Titulo t3 = Titulo.builder().cliente(c3).descricao("Fornecimento Mensal").valorOriginal(new BigDecimal("850.00")).dataVencimento(LocalDate.now().plusDays(5)).status(StatusTitulo.PENDENTE).build();
            Titulo t4 = Titulo.builder().cliente(c1).descricao("Licença de Software").valorOriginal(new BigDecimal("4500.00")).dataVencimento(LocalDate.now().plusDays(12)).status(StatusTitulo.PENDENTE).build();

            // Atrasados (Inadimplência - Vermelho)
            Titulo t5 = Titulo.builder().cliente(c2).descricao("Gestão de Tráfego").valorOriginal(new BigDecimal("2100.00")).dataVencimento(LocalDate.now().minusDays(3)).status(StatusTitulo.PENDENTE).build();

            tituloRepository.saveAll(Arrays.asList(t1, t2, t3, t4, t5));

            // 3. Criando Logs do Robô para aparecer no Feed da Home
            LogDisparo log1 = LogDisparo.builder().cliente(c2).tipoRegua("PREVENTIVO").assunto("Lembrete: Fatura vencendo em breve").dataHoraEnvio(LocalDateTime.now().minusDays(2)).build();
            LogDisparo log2 = LogDisparo.builder().cliente(c2).tipoRegua("ATRASO").assunto("Aviso de Atraso: Fatura pendente").dataHoraEnvio(LocalDateTime.now().minusHours(5)).build();
            LogDisparo log3 = LogDisparo.builder().cliente(c3).tipoRegua("HOJE").assunto("Sua fatura vence hoje").dataHoraEnvio(LocalDateTime.now().minusMinutes(30)).build();

            logDisparoRepository.saveAll(Arrays.asList(log1, log2, log3));

            System.out.println("✅ Banco populado com sucesso! Dashboard pronto para apresentação.");
        }
    }
}