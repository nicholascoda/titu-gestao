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
    public void salvar(Titulo tituloForm, Long clienteId, String tipoCobranca, java.math.BigDecimal valorInformado, Integer quantidade) {
        Cliente cliente = clienteService.buscarPorId(clienteId);

        if (tituloForm.getId() != null) {
            // ================= EDITAR =================
            Titulo tituloFinal = tituloRepository.findById(tituloForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Título não encontrado"));

            tituloFinal.setDescricao(tituloForm.getDescricao());
            tituloFinal.setValorOriginal(tituloForm.getValorOriginal());
            tituloFinal.setDataVencimento(tituloForm.getDataVencimento());
            tituloFinal.setCliente(cliente);
            tituloFinal.setObservacao(tituloForm.getObservacao());

            if (tituloFinal.getStatus() == StatusTitulo.PAGO && tituloFinal.getDataPagamento() == null) {
                tituloFinal.setDataPagamento(LocalDate.now());
            }

            tituloRepository.save(tituloFinal);
            logService.registrarAcao("EDITAR", "Cobrança atualizada: " + tituloFinal.getDescricao());

        } else {
            // ================= CRIAR (Motor Híbrido com BigDecimal) =================
            int parcelas = (quantidade != null && quantidade > 0) ? quantidade : 1;

            // Pega o valor da tela. Se for nulo, pega zero.
            java.math.BigDecimal valorBase = (valorInformado != null) ? valorInformado :
                    (tituloForm.getValorOriginal() != null ? tituloForm.getValorOriginal() : java.math.BigDecimal.ZERO);

            java.math.BigDecimal valorPorBoleto = valorBase;

            // Matemática financeira de verdade: divide e arredonda as casas decimais
            if ("PARCELADA".equals(tipoCobranca)) {
                valorPorBoleto = valorBase.divide(new java.math.BigDecimal(parcelas), 2, java.math.RoundingMode.HALF_UP);
            }

            for (int i = 0; i < parcelas; i++) {
                Titulo novaParcela = new Titulo();
                novaParcela.setCliente(cliente);

                String sufixo = (parcelas > 1) ? " (" + (i + 1) + "/" + parcelas + ")" : "";
                novaParcela.setDescricao(tituloForm.getDescricao() + sufixo);

                // Agora o Java aceita sem reclamar!
                novaParcela.setValorOriginal(valorPorBoleto);

                novaParcela.setDataVencimento(tituloForm.getDataVencimento().plusMonths(i));
                novaParcela.setObservacao(tituloForm.getObservacao());
                novaParcela.setStatus(StatusTitulo.PENDENTE);

                tituloRepository.save(novaParcela);
            }

            String msgLog = (parcelas > 1) ? " gerados em lote" : " gerado";
            logService.registrarAcao("CRIAR", parcelas + " títulos" + msgLog + " para " + cliente.getNomeEmpresa() + " - " + tituloForm.getDescricao());
        }
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