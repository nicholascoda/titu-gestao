package com.titu.core.service;

import com.titu.core.model.Cliente;
import com.titu.core.model.StatusTitulo;
import com.titu.core.model.Titulo;
import com.titu.core.repository.TituloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.titu.core.service.LogAcaoService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TituloService {

    private final TituloRepository tituloRepository;
    private final ClienteService clienteService;
    private final LogAcaoService logService;

    @Transactional
    public Titulo salvar(Titulo tituloForm, Long clienteId) {
        Cliente cliente = clienteService.buscarPorId(clienteId);
        Titulo tituloFinal;

        // 0. DESCOBRIR A AÇÃO
        // Se já tem ID no formulário, é uma edição. Se não tem, é criação.
        String acao = (tituloForm.getId() != null) ? "EDITAR" : "CRIAR";

        if (tituloForm.getId() != null) {
            // EDITAR
            tituloFinal = tituloRepository.findById(tituloForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

            tituloFinal.setDescricao(tituloForm.getDescricao());
            tituloFinal.setValorOriginal(tituloForm.getValorOriginal());
            tituloFinal.setDataVencimento(tituloForm.getDataVencimento());
            tituloFinal.setCliente(cliente);

        } else {
            // CRIAR
            tituloFinal = tituloForm;
            tituloFinal.setCliente(cliente);
            tituloFinal.setStatus(StatusTitulo.PENDENTE);
        }

        if (tituloFinal.getStatus() == StatusTitulo.PAGO && tituloFinal.getDataPagamento() == null) {
            tituloFinal.setDataPagamento(LocalDate.now());
        }

        // 1. SALVAR NO BANCO
        Titulo tituloSalvo = tituloRepository.save(tituloFinal);

        // 2. GRAVAR O LOG
        logService.registrarAcao(acao, "Cobrança de R$ " + tituloSalvo.getValorOriginal() + " (" + tituloSalvo.getDescricao() + ") para " + cliente.getNomeEmpresa());

        return tituloSalvo;
    }

    public List<Titulo> listarTodos() {
        return tituloRepository.findAllComCliente();    }

    public void excluir(Long id) {
        // 1. busca o título ANTES de apagar, para a gente saber o nome/descrição dele pro Log
        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

        // 2. apaga do banco
        tituloRepository.deleteById(id);

        // 3. grava o Log com a fofoca completa
        logService.registrarAcao("EXCLUIR", "Cobrança removida: " + titulo.getDescricao() + " (Cliente: " + titulo.getCliente().getNomeEmpresa() + ")");
    }

    public void darBaixa(Long id) {
        Titulo titulo = tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

        titulo.setStatus(StatusTitulo.PAGO);
        tituloRepository.save(titulo);

        // log do pagamento
        logService.registrarAcao("PAGAMENTO", "Baixa confirmada: " + titulo.getDescricao() + " (Cliente: " + titulo.getCliente().getNomeEmpresa() + ")");
    }

    public Titulo buscarPorId(Long id) {
        return tituloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Título não encontrado com ID: " + id));
    }

    public List<Titulo> listarComFiltro(String filtro) {
        if ("pagos".equals(filtro)) {
            return tituloRepository.findAllByStatus(StatusTitulo.PAGO);
        } else if ("pendentes".equals(filtro)) {
            return tituloRepository.findAllByStatus(StatusTitulo.PENDENTE);
        } else if ("vencidos".equals(filtro)) {
            return tituloRepository.findVencidos();
        } else {
            return tituloRepository.findAllComCliente();
        }
    }

    public List<Titulo> listarPorCliente(Long clienteId) {
        return tituloRepository.buscarPorCliente(clienteId);
    }

}