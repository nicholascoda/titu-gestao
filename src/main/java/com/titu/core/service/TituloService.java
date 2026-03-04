package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.model.StatusTitulo;
import com.titu.core.model.Titulo;
import com.titu.core.repository.TituloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TituloService {

    private final TituloRepository tituloRepository;
    private final ClienteService clienteService; // Vamos usar o service do cliente pra buscar ele!

    @Transactional
    public Titulo salvar(Titulo tituloForm, Long clienteId) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        Titulo tituloFinal;

        if (tituloForm.getId() != null) {
            // --- ATUALIZAÇÃO (EDITAR) ---
            // 1. Busca o original no banco (com todas as proteções e versões)
            tituloFinal = tituloRepository.findById(tituloForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

            // 2. Atualiza só os dados permitidos
            tituloFinal.setDescricao(tituloForm.getDescricao());
            tituloFinal.setValorOriginal(tituloForm.getValorOriginal());
            tituloFinal.setDataVencimento(tituloForm.getDataVencimento());
            tituloFinal.setCliente(cliente); // Atualiza o cliente caso tenha mudado

            // Obs: Não mexemos no status nem na data de pagamento na edição simples
        } else {
            // --- CRIAÇÃO (NOVO) ---
            tituloFinal = tituloForm;
            tituloFinal.setCliente(cliente);
            tituloFinal.setStatus(StatusTitulo.PENDENTE); // Padrão pra novos
        }

        // Regra de segurança para Status PAGO
        if (tituloFinal.getStatus() == StatusTitulo.PAGO && tituloFinal.getDataPagamento() == null) {
            tituloFinal.setDataPagamento(LocalDate.now());
        }

        return tituloRepository.save(tituloFinal);
    }

    public List<Titulo> listarTodos() {
        return tituloRepository.findAllComCliente();    }

    public void excluir(Long id) {
        tituloRepository.deleteById(id);
    }

    // Importe o StatusTitulo se precisar
    public void darBaixa(Long id) {
        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

        titulo.setStatus(StatusTitulo.PAGO); // Troca o selo
        tituloRepository.save(titulo);       // Salva a alteração
    }

    public Titulo buscarPorId(Long id) {
        return tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Título não encontrado com ID: " + id));
    }

    // Metodo Inteligente de Listagem
    public List<Titulo> listarComFiltro(String filtro) {
        if ("pagos".equals(filtro)) {
            return tituloRepository.findAllByStatus(StatusTitulo.PAGO);
        } else if ("pendentes".equals(filtro)) {
            return tituloRepository.findAllByStatus(StatusTitulo.PENDENTE);
        } else if ("vencidos".equals(filtro)) {
            return tituloRepository.findVencidos();
        } else {
            // Se não tiver filtro (ou filtro desconhecido), traz tudo
            return tituloRepository.findAllComCliente();
        }
    }

    public List<Titulo> listarPorCliente(Long clienteId) {
        return tituloRepository.buscarPorCliente(clienteId);
    }

}