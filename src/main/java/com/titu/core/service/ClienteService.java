package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // O Lombok cria o construtor pra injetar o Repository
public class ClienteService {

    private final ClienteRepository repository;

    @Transactional // Garante que ou salva tudo ou não salva nada (Rollback)
    public Cliente salvar(Cliente cliente) {

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
        // Regra 1: Não pode duplicar E-mail
        if (repository.existsByEmail(cliente.getEmail())) {
            throw new IllegalArgumentException("Já existe um cliente com este e-mail.");
        }

        // Regra 2: Se tiver CNPJ, não pode duplicar também
        // (Agora ele vai checar usando o número limpo!)
        if (cliente.getCnpj() != null && !cliente.getCnpj().isBlank()) {
            if (repository.existsByCnpj(cliente.getCnpj())) {
                throw new IllegalArgumentException("Já existe um cliente com este CNPJ.");
            }
        }

        // --- 3. SALVAR ---
        return repository.save(cliente);
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + id));
    }

    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
