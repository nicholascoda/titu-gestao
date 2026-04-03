package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.model.Titulo;
import com.titu.core.repository.TituloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TituloServiceTest {

    // 1. Criando os "Dublês" do Banco de Dados e outros Services
    @Mock
    private TituloRepository tituloRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private LogAcaoService logService;

    // 2. Injetando os dublês dentro do TituloService real
    @InjectMocks
    private TituloService tituloService;

    private Cliente clienteFake;
    private Titulo tituloForm;

    @BeforeEach
    void setUp() {
        // Prepara os dados falsos antes de cada teste rodar
        clienteFake = new Cliente();
        clienteFake.setId(1L);
        clienteFake.setNomeEmpresa("Empresa Teste");

        tituloForm = new Titulo();
        tituloForm.setDescricao("Desenvolvimento de Site");
        tituloForm.setDataVencimento(LocalDate.now());

        // Ensina o dublê: "Quando buscarem o cliente 1, devolva o clienteFake"
        lenient().when(clienteService.buscarPorId(1L)).thenReturn(clienteFake);
    }

    @Test
    void deveSalvarCobrancaUnica() {
        // Executa o cenário: Valor de 1000 à vista
        tituloService.salvar(tituloForm, 1L, "UNICA", new BigDecimal("1000.00"), 1);

        // Verifica se o repository.save foi chamado EXATAMENTE 1 vez
        verify(tituloRepository, times(1)).save(any(Titulo.class));
        verify(logService, times(1)).registrarAcao(eq("CRIAR"), anyString());
    }

    @Test
    void deveSalvarCobrancaParcelada_DividindoOValor() {
        // Executa o cenário: R$ 1000 dividido em 5 vezes
        tituloService.salvar(tituloForm, 1L, "PARCELADA", new BigDecimal("1000.00"), 5);

        // O ArgumentCaptor é um "detetive" que pesca o que foi mandado pro banco
        ArgumentCaptor<Titulo> captor = ArgumentCaptor.forClass(Titulo.class);

        // Verifica se salvou 5 vezes e captura os 5 objetos gerados
        verify(tituloRepository, times(5)).save(captor.capture());
        List<Titulo> titulosSalvos = captor.getAllValues();

        // Fazemos a auditoria da matemática
        assertEquals(5, titulosSalvos.size(), "Deveria ter gerado 5 boletos");

        // 1000 / 5 = 200 cravado
        assertEquals(new BigDecimal("200.00"), titulosSalvos.get(0).getValorOriginal());

        // Verifica se os sufixos (1/5) e (5/5) foram colocados no nome
        assertEquals("Desenvolvimento de Site (1/5)", titulosSalvos.get(0).getDescricao());
        assertEquals("Desenvolvimento de Site (5/5)", titulosSalvos.get(4).getDescricao());

        // Verifica se o vencimento da 2ª parcela andou 1 mês pra frente certinho
        assertEquals(LocalDate.now().plusMonths(1), titulosSalvos.get(1).getDataVencimento());
    }

    @Test
    void deveSalvarCobrancaRecorrente_MantendoOValor() {
        // Executa o cenário: R$ 100 repetido por 3 meses (Assinatura)
        tituloService.salvar(tituloForm, 1L, "RECORRENTE", new BigDecimal("100.00"), 3);

        ArgumentCaptor<Titulo> captor = ArgumentCaptor.forClass(Titulo.class);
        verify(tituloRepository, times(3)).save(captor.capture());
        List<Titulo> titulosSalvos = captor.getAllValues();

        assertEquals(3, titulosSalvos.size(), "Deveria ter gerado 3 boletos");

        // Na recorrência, o valor NÃO divide! A primeira e a última tem que ser 100.
        assertEquals(new BigDecimal("100.00"), titulosSalvos.get(0).getValorOriginal());
        assertEquals(new BigDecimal("100.00"), titulosSalvos.get(2).getValorOriginal());
    }

    // ================= TESTES DO CAMINHO TRISTE (SAD PATH) =================

    @Test
    void deveLancarExcecaoAoTentarEditarTituloInexistente() {
        // Prepara a armadilha: Um título com ID 999 (que não existe no banco)
        Titulo tituloEditado = new Titulo();
        tituloEditado.setId(999L);
        tituloEditado.setDescricao("Hackeando o sistema");

        // Ensina o dublê: "Se procurarem o ID 999, devolva VAZIO"
        when(tituloRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Confere se o Java REALMENTE lança a exceção esperada e barra a operação
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.salvar(tituloEditado, 1L, "UNICA", new BigDecimal("100.00"), 1)
        );

        // Verifica se a mensagem de erro é exatamente a que você escreveu no código
        assertEquals("Título não encontrado", erro.getMessage());

        // Garante que NENHUM salvamento foi feito no banco
        verify(tituloRepository, never()).save(any(Titulo.class));
    }

    @Test
    void deveLancarExcecaoAoTentarExcluirTituloInexistente() {
        when(tituloRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> tituloService.excluir(999L)
        );

        // Garante que não tentou apagar nada no banco
        verify(tituloRepository, never()).deleteById(anyLong());
    }

    @Test
    void deveForcarCobrancaAVistaSeQuantidadeDeParcelasForInvalida() {
        // Cenário: Tentaram mandar -5 parcelas na maldade
        tituloService.salvar(tituloForm, 1L, "PARCELADA", new BigDecimal("500.00"), -5);

        // O sistema DEVE ignorar o -5, forçar para 1 parcela (à vista) e salvar só 1 vez
        verify(tituloRepository, times(1)).save(any(Titulo.class));
    }


}