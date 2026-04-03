package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

// Importações estáticas para deixar o código mais limpo (Padrão Sênior)
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository repository;

    @Mock
    private LogAcaoService logService;

    @InjectMocks
    private ClienteService clienteService;

    // ================= CAMINHOS FELIZES =================

    @Test
    void deveSalvarClienteComSucessoQuandoEmailEstiverLivre() {
        Cliente clienteNovo = new Cliente();
        clienteNovo.setEmail("sucesso@empresa.com");
        clienteNovo.setNomeEmpresa("Empresa do Sucesso");

        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cliente.class))).thenReturn(clienteNovo);

        Cliente resultado = clienteService.salvar(clienteNovo);

        assertNotNull(resultado);
        assertEquals("sucesso@empresa.com", resultado.getEmail());

        verify(repository, times(1)).save(clienteNovo);
        verify(logService, times(1)).registrarAcao(anyString(), anyString());
    }

    @Test
    void devePermitirAtualizarClienteSeOEmailForDeleMesmo() {
        Cliente clienteDaTela = new Cliente();
        clienteDaTela.setId(1L);
        clienteDaTela.setEmail("carlos@techsolutions.com");
        clienteDaTela.setNomeEmpresa("Carlos Nova Empresa S/A");

        Cliente clienteDoBanco = new Cliente();
        clienteDoBanco.setId(1L);
        clienteDoBanco.setEmail("carlos@techsolutions.com");
        clienteDoBanco.setNomeEmpresa("Carlos Antiga Empresa");

        when(repository.findByEmail("carlos@techsolutions.com")).thenReturn(Optional.of(clienteDoBanco));
        when(repository.save(any(Cliente.class))).thenReturn(clienteDaTela);

        Cliente resultado = clienteService.salvar(clienteDaTela);

        assertNotNull(resultado);
        assertEquals("Carlos Nova Empresa S/A", resultado.getNomeEmpresa());
        assertEquals(1L, resultado.getId());

        verify(repository, times(1)).save(clienteDaTela);
    }

    // ================= SAD PATHS =================

    @Test
    void naoDeveSalvarClienteNovoSeEmailJaExistir() {
        Cliente clienteNovo = new Cliente();
        clienteNovo.setEmail("carlos@techsolutions.com");
        clienteNovo.setNomeEmpresa("Nova Empresa");

        Cliente clienteAntigo = new Cliente();
        clienteAntigo.setId(1L);
        clienteAntigo.setEmail("carlos@techsolutions.com");

        when(repository.findByEmail(anyString())).thenReturn(Optional.of(clienteAntigo));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.salvar(clienteNovo);
        });

        assertEquals("Já existe um cliente cadastrado com este e-mail.", erro.getMessage());
        verify(repository, never()).save(any(Cliente.class)); // Garante que foi barrado antes de salvar
    }

    @Test
    void naoDevePermitirAtualizarRoubandoEmailDeOutroCliente() {
        // Cliente da tela (ID 1) quer mudar seu e-mail para o da Apple
        Cliente clienteDaTela = new Cliente();
        clienteDaTela.setId(1L);
        clienteDaTela.setEmail("steve@apple.com");

        // Mas o banco diz que o e-mail da Apple já pertence ao cliente ID 2!
        Cliente clienteDoBanco = new Cliente();
        clienteDoBanco.setId(2L); // ID DIFERENTE DA TELA!
        clienteDoBanco.setEmail("steve@apple.com");

        when(repository.findByEmail("steve@apple.com")).thenReturn(Optional.of(clienteDoBanco));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> {
            clienteService.salvar(clienteDaTela);
        });

        assertEquals("Já existe um cliente cadastrado com este e-mail.", erro.getMessage());
        verify(repository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        // Prepara a armadilha: ID 99 não existe
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.buscarPorId(99L);
        });
    }

    @Test
    void deveLancarExcecaoAoTentarExcluirClienteInexistente() {
        // Ensina o dublê que não existe ninguém com ID 99
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            clienteService.excluir(99L);
        });

        // Garante que o comando de deletar do banco nunca foi chamado
        verify(repository, never()).deleteById(anyLong());
    }
}