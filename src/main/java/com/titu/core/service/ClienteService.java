package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.titu.core.service.LogAcaoService;

import java.util.List;

@Service
@RequiredArgsConstructor // O Lombok cria o construtor pra injetar o Repository
public class ClienteService {

    private final ClienteRepository repository;
    private final LogAcaoService logService;

    @Transactional // Garante que ou salva tudo ou não salva nada (Rollback)
    public Cliente salvar(Cliente cliente) {

        // --- 0. DESCOBRIR A AÇÃO (O que resolve o erro vermelho!) ---
        // Se o ID for null, é um cliente novo. Se já tiver ID, é uma edição.
        String acao = (cliente.getId() == null) ? "CRIAR" : "EDITAR";

        // --- 1. FAXINA (Limpeza de formatação antes de tudo) ---
        if (cliente.getCnpj() != null && !cliente.getCnpj().isBlank()) {
            String cnpjLimpo = cliente.getCnpj().replaceAll("\\D", "");
            cliente.setCnpj(cnpjLimpo);
        }

        if (cliente.getTelefone() != null && !cliente.getTelefone().isBlank()) {
            String telefoneLimpo = cliente.getTelefone().replaceAll("\\D", "");
            cliente.setTelefone(telefoneLimpo);
        }

        // --- 2. VALIDAÇÕES ---
        // Regra 1: Não pode duplicar E-mail (Mas permite se for o próprio cliente editando)
        java.util.Optional<Cliente> clientePorEmail = repository.findByEmail(cliente.getEmail());
        if (clientePorEmail.isPresent() && !clientePorEmail.get().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este e-mail.");
        }

        // Regra 2: Se tiver CNPJ, não pode duplicar também (Mas permite o próprio cliente)
        if (cliente.getCnpj() != null && !cliente.getCnpj().isBlank()) {
            java.util.Optional<Cliente> clientePorCnpj = repository.findByCnpj(cliente.getCnpj());
            if (clientePorCnpj.isPresent() && !clientePorCnpj.get().getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("Já existe um cliente cadastrado com este CNPJ.");
            }
        }

        // --- 3. SALVAR NO BANCO ---
        // Salva primeiro e guarda a resposta na variável 'clienteSalvo'
        Cliente clienteSalvo = repository.save(cliente);

        // --- 4. GRAVAR O LOG (Somente se o salvamento deu certo) ---
        logService.registrarAcao(acao, "Cliente: " + clienteSalvo.getNomeEmpresa());

        // Retorna o cliente que acabou de ser salvo e logado
        return clienteSalvo;
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));
    }

    public void excluir(Long id) {
        // Pega o nome do cliente antes de apagar para ficar bonito no log
        Cliente cliente = buscarPorId(id);

        repository.deleteById(id);

        // Gravando o Log de exclusão!
        logService.registrarAcao("EXCLUIR", "Cliente removido: " + cliente.getNomeEmpresa());
    }
}
