package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class) // Avisa pro Spring que vamos usar o Mockito (os dublês)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository; // O nosso dublê do banco de dados!

    @Mock
    private LogAcaoService logService; // Outro dublê, para não tentar gravar logs reais e dar erro

    @InjectMocks
    private ClienteService clienteService; // O verdadeiro Service que queremos testar (o alvo)

    @Test
    void naoDeveSalvarClienteSeEmailJaExistir() {
        // 1. ARRANGE (PREPARAR O CENÁRIO)
        // Criamos um cliente falso que supostamente veio da tela HTML (sem ID, porque é novo)
        Cliente clienteNovo = new Cliente();
        clienteNovo.setEmail("carlos@techsolutions.com");
        clienteNovo.setNomeEmpresa("Nova Empresa");

        // Criamos um cliente que "já estava no banco de dados" (com ID 1)
        Cliente clienteAntigo = new Cliente();
        clienteAntigo.setId(1L);
        clienteAntigo.setEmail("carlos@techsolutions.com");

        // Ensinamos o dublê a mentir: "Quando o Service te pedir um e-mail, devolve o clienteAntigo!"
        Mockito.when(repository.findByEmail(anyString())).thenReturn(Optional.of(clienteAntigo));

        // 2 & 3. ACT & ASSERT (AGIR E VERIFICAR)
        // Pedimos pro JUnit garantir que, ao tentar salvar o cliente novo, uma exceção seja estourada!
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.salvar(clienteNovo);
        });

        // Verificamos se a mensagem do erro é exatamente aquela que você escreveu no código original
        assertEquals("Já existe um cliente cadastrado com este e-mail.", erro.getMessage());
    }

    @Test
    void deveSalvarClienteComSucessoQuandoEmailEstiverLivre() {
        // 1. ARRANGE (PREPARAR)
        // Criamos o cliente novinho em folha
        Cliente clienteNovo = new Cliente();
        clienteNovo.setEmail("sucesso@empresa.com");
        clienteNovo.setNomeEmpresa("Empresa do Sucesso");

        // Ensinamos o dublê a dar a luz verde: "Se procurarem esse e-mail, diz que NÃO EXISTE (vazio)!"
        Mockito.when(repository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Ensinamos o dublê do banco a fingir que salvou e devolver o próprio cliente
        Mockito.when(repository.save(Mockito.any(Cliente.class))).thenReturn(clienteNovo);

        // 2. ACT (AGIR)
        // Aqui não tem armadilha (assertThrows) porque não queremos erro!
        // Queremos apenas chamar o metodo e guardar o resultado.
        Cliente resultado = clienteService.salvar(clienteNovo);

        // 3. ASSERT (VERIFICAR)
        // Garantimos que o serviço nos devolveu um cliente de verdade e não um 'null'
        assertNotNull(resultado);
        assertEquals("sucesso@empresa.com", resultado.getEmail());

        // --- AQUI ESTÁ O OURO DO CAMINHO FELIZ ---
        // Nós viramos pro dublê do banco e perguntamos: "Vem cá, o Service TE CHAMOU pra salvar?"
        Mockito.verify(repository, Mockito.times(1)).save(clienteNovo);

        // Nós viramos pro dublê de logs e perguntamos: "O Service TE CHAMOU pra registrar a ação?"
        Mockito.verify(logService, Mockito.times(1))
                .registrarAcao(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void devePermitirAtualizarClienteSeOEmailForDeleMesmo() {
        // 1. ARRANGE (O Figurino e o Roteiro)

        // O cliente que veio da tela HTML, querendo mudar apenas o nome,
        // mas mantendo o mesmo e-mail e o mesmo ID (ID 1L)
        Cliente clienteDaTela = new Cliente();
        clienteDaTela.setId(1L);
        clienteDaTela.setEmail("carlos@techsolutions.com");
        clienteDaTela.setNomeEmpresa("Carlos Nova Empresa S/A"); // <- Mudou o nome

        // O cliente como ele está gravado no banco de dados hoje
        Cliente clienteDoBanco = new Cliente();
        clienteDoBanco.setId(1L); // <- O ID é o mesmo!
        clienteDoBanco.setEmail("carlos@techsolutions.com");
        clienteDoBanco.setNomeEmpresa("Carlos Antiga Empresa");

        // O Dublê do Banco: "Quando buscarem o e-mail do Carlos, devolve o cliente do banco"
        Mockito.when(repository.findByEmail("carlos@techsolutions.com")).thenReturn(Optional.of(clienteDoBanco));

        // O Dublê do Banco: "Quando mandarem salvar, aceita e devolve o cliente da tela"
        Mockito.when(repository.save(Mockito.any(Cliente.class))).thenReturn(clienteDaTela);

        // 2. ACT (Ação!)
        Cliente resultado = clienteService.salvar(clienteDaTela);

        // 3. ASSERT (Verificar o replay da cena)
        // Garantimos que não deu erro e que ele salvou o nome novo
        assertNotNull(resultado);
        assertEquals("Carlos Nova Empresa S/A", resultado.getNomeEmpresa());
        assertEquals(1L, resultado.getId());

        // Interrogatório: Garantimos que o dublê do banco foi chamado para salvar
        Mockito.verify(repository, Mockito.times(1)).save(clienteDaTela);
    }



}