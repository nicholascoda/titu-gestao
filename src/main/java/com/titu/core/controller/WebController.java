package com.titu.core.controller;

import com.titu.core.model.Cliente;
import com.titu.core.model.Titulo;
import com.titu.core.repository.TituloRepository;
import com.titu.core.service.ClienteService;
import com.titu.core.service.EmailService;
import com.titu.core.service.TituloService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller; // <--- Note que é @Controller, não @RestController
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebController {

    //ClienteClontroller retorna JSon pro postman, esse WebController retorna HTML pro navegador

    private final ClienteService clienteService;
    private final TituloService tituloService;
    private final EmailService emailService;
    private final TituloRepository  tituloRepository;
    private final com.titu.core.repository.LogAcaoRepository logAcaoRepository;
    private final com.titu.core.service.ConfiguracaoService configuracaoService;
    private final com.titu.core.repository.ClienteRepository clienteRepository;
    private final com.titu.core.repository.LogDisparoRepository logDisparoRepository;


    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String mesBusca, Model model) {
        // 1. Se o usuário não escolheu nenhum mês, pega o mês atual
        if (mesBusca == null || mesBusca.isEmpty()) {
            mesBusca = java.time.YearMonth.now().toString();
        }

        // 2. Manda para o HTML qual mês está selecionado para o calendário preencher certo
        model.addAttribute("mesSelecionado", mesBusca);

        // 3. Calcula o início e o fim do mês (Já deixando engatilhado para filtrar o banco)
        java.time.YearMonth anoMes = java.time.YearMonth.parse(mesBusca);
        java.time.LocalDate inicioDoMes = anoMes.atDay(1);
        java.time.LocalDate fimDoMes = anoMes.atEndOfMonth();

        // -------------------------------------------------------------------------

        Double pendenteMes = tituloRepository.somarTotalPendentePorPeriodo(inicioDoMes, fimDoMes);
        Double pagoMes = tituloRepository.somarTotalPagoPorPeriodo(inicioDoMes, fimDoMes);
        Long vencidosMes = tituloRepository.contarVencidosPorPeriodo(inicioDoMes, fimDoMes);

        // Se o banco não achar nada no mês, ele devolve nulo para evitar dar erro na tela
        model.addAttribute("totalPendente", pendenteMes != null ? pendenteMes : 0.0);
        model.addAttribute("totalPago", pagoMes != null ? pagoMes : 0.0);
        model.addAttribute("qtdVencidos", vencidosMes != null ? vencidosMes : 0L);


        model.addAttribute("qtdClientes", clienteService.listarTodos().size());

        model.addAttribute("ultimosDisparos", logDisparoRepository.findTop5ByOrderByDataHoraEnvioDesc());

        return "home";
    }


    @GetMapping("/clientes")
    public String paginaClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        // Envia um objeto vazio para o formulário poder validar os campos
        model.addAttribute("cliente", new Cliente());
        return "clientes";
    }

    // 2. ROTA PARA SALVAR (Com Validação Inteligente)
    @PostMapping("/clientes/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {

        try {
            // Tenta salvar (tanto faz se é novo ou edição)
            clienteService.salvar(cliente);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente salvo com sucesso!");

        } catch (IllegalArgumentException e) {
            // Se o Service reclamar (ex: E-mail já existe), mostra erro vermelho
            redirectAttributes.addFlashAttribute("mensagemErro", "Atenção: " + e.getMessage());

        } catch (Exception e) {
            // Se der qualquer outro erro maluco no banco
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro inesperado ao salvar cliente.");
        }

        // Redireciona limpando a URL e fechando os modais
        return "redirect:/clientes";
    }
    @GetMapping("/titulos")
    @Transactional
    public String paginaTitulos(@RequestParam(required = false) String filtro, Model model) {
        // chama o metodo com filtro
        List<Titulo> lista = tituloService.listarComFiltro(filtro);

        model.addAttribute("titulos", lista);
        model.addAttribute("clientes", clienteService.listarTodos());

        // Passa o filtro de volta pra tela (pra saber o que está vendo)
        model.addAttribute("filtroAtivo", filtro);

        return "titulos";
    }

    // ROTA PARA SALVAR (Vem do Formulário)
    @PostMapping("/titulos/salvar")
    public String salvarTitulo(Titulo titulo, Long clienteId) {
        // Mudou de .criarNovoTitulo(...) para .salvar(...)
        tituloService.salvar(titulo, clienteId);
        return "redirect:/titulos";
    }

// --- ROTAS DE EXCLUSÃO ---

    @GetMapping("/clientes/excluir/{id}")
    public String excluirCliente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clienteService.excluir(id);
            // Mensagem de sucesso
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // Se der erro de chave estrangeira, tem dívidas
            redirectAttributes.addFlashAttribute("mensagemErro", "Não é possível excluir: Este cliente possui cobranças vinculadas! Apague as cobranças primeiro.");
        } catch (Exception e) {
            // Qualquer outro erro genérico
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir cliente: " + e.getMessage());
        }

        return "redirect:/clientes";
    }

    @GetMapping("/titulos/excluir/{id}")
    public String excluirTitulo(@PathVariable Long id) {
        tituloService.excluir(id);
        return "redirect:/titulos";
    }

    // Rota de Teste
    @GetMapping("/teste-email")
    public String testeEmail() {
        emailService.enviarEmailSimples(
                "teste@cliente.com",
                "Olá do Titu!",
                "Se você recebeu isso, o Mailtrap está funcionando!"
        );
        return "redirect:/";
    }

    @GetMapping("/titulos/pagar/{id}")
    public String darBaixaTitulo(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        // 1. Dá a baixa no banco e prepara a mensagem
        tituloService.darBaixa(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Pagamento confirmado com sucesso!");

        // 2. Pegar a URL exata de onde o clique veio, com os filtros
        String urlAnterior = request.getHeader("Referer");

        // 3. Redirecionar de volta. Se por acaso a URL sumir, ele volta pro padrão.
        if (urlAnterior != null) {
            return "redirect:" + urlAnterior;
        } else {
            return "redirect:/titulos";
        }
    }

    @GetMapping("/titulos/editar/{id}")
    public String editarTitulo(@PathVariable Long id, Model model) {
        Titulo titulo = tituloService.buscarPorId(id);

        model.addAttribute("titulo", titulo);
        model.addAttribute("clientes", clienteService.listarTodos());

        return "titulo-editar"; //
    }

    @GetMapping("/suporte")
    public String suporte() {
        return "suporte";
    }


    @GetMapping("/clientes/{id}/detalhes")
    public String detalhesCliente(@PathVariable Long id, Model model) {
        // Busca o cliente para mostrar os dados dele no topo
        model.addAttribute("cliente", clienteService.buscarPorId(id));

        // Busca as dívidas ordenadas (Pendente primeiro)
        model.addAttribute("titulos", tituloService.listarPorCliente(id));

        // Busca o histórico da Caixa Preta do Titu-Bô
        model.addAttribute("historicoEmails", logDisparoRepository.findByClienteIdOrderByDataHoraEnvioDesc(id));

        return "cliente-detalhes";
    }

    @GetMapping("/configuracoes")
    public String paginaConfiguracoes(Model model) {
        // Pega a configuração atual. Se não existir, ele cria a DEFAULT na hora
        com.titu.core.model.ConfiguracaoRobo configRobo = configuracaoService.obterConfiguracaoAtual();

        // Manda pro HTML
        model.addAttribute("configRobo", configRobo);

        return "configuracoes";
    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Vai chamar o nosso login.html
    }

    @GetMapping("/titulos/estornar/{id}")
    public String estornarPagamento(@PathVariable Long id) {
        // 1. Busca o título pelo ID
        com.titu.core.model.Titulo titulo = tituloRepository.findById(id).orElse(null);

        if (titulo != null) {
            // 2. Volta o status para PENDENTE
            titulo.setStatus(com.titu.core.model.StatusTitulo.PENDENTE);

            // 3. Salva a alteração
            tituloRepository.save(titulo);
        }

        // 4. Volta para a tela de títulos automaticamente
        return "redirect:/titulos";
    }

    // --- 2. ROTA PARA ATUALIZAR O PERFIL ---
    @PostMapping("/perfil/atualizar")
    public String atualizarPerfil(String nome, String senha, RedirectAttributes redirectAttributes) {


        redirectAttributes.addFlashAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");
        return "redirect:/configuracoes";
    }
    // --- 3. ROTA PARA SALVAR A EMPRESA ---
    @PostMapping("/configuracoes/empresa/salvar")
    public String salvarEmpresa(String nomeFantasia, String cnpj, String telefone, String email, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados da empresa atualizados com sucesso!");
        redirectAttributes.addFlashAttribute("abaAtiva", "empresa");
        return "redirect:/configuracoes";
    }

    @PostMapping("/configuracoes/automacoes/salvar")
    public String salvarAutomacoes(com.titu.core.model.ConfiguracaoRobo formRobo, RedirectAttributes redirectAttributes) {

        com.titu.core.model.ConfiguracaoRobo configAtual = configuracaoService.obterConfiguracaoAtual();
        formRobo.setId(configAtual.getId());

        // Salva as novas escolhas do usuário
        configuracaoService.salvarConfiguracao(formRobo);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "As regras do Robô foram atualizadas!");
        redirectAttributes.addFlashAttribute("abaAtiva", "automacoes"); // Fofoca pra manter na aba certa

        return "redirect:/configuracoes";
    }



}