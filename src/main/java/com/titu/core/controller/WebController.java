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

    @ModelAttribute("currentUri")
    public String getCurrentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @GetMapping("/")
    public String home(Model model) {
        // Busca os indicadores no banco
        model.addAttribute("totalPendente", tituloRepository.somarTotalPendente());
        model.addAttribute("totalPago", tituloRepository.somarTotalPago());
        model.addAttribute("qtdClientes", clienteService.listarTodos().size());
        model.addAttribute("qtdVencidos", tituloRepository.contarVencidos());

        return "home";
    }


    @GetMapping("/clientes")
    public String paginaClientes(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        // Enviamos um objeto vazio para o formulário poder validar os campos
        model.addAttribute("cliente", new Cliente());
        return "clientes";
    }

    // 2. ROTA PARA SALVAR (Com Validação Inteligente)
    @PostMapping("/clientes/salvar")
    public String salvarCliente(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result, Model model) {

        // Se tiver erro básico (ex: campo vazio)
        if (result.hasErrors()) {
            // Recarrega a lista de baixo (se não ela some)
            model.addAttribute("clientes", clienteService.listarTodos());
            // Avisa pro HTML que o modal tem que ficar aberto
            model.addAttribute("modalAberto", true);
            return "clientes"; // Volta pra mesma tela (não faz redirect)
        }

        try {
            clienteService.salvar(cliente);
        } catch (IllegalArgumentException e) {
            // Se der erro de duplicação (Regra de Negócio do Service)
            // Jogamos o erro para o campo "email" (ou geral)
            result.rejectValue("email", "error.cliente", e.getMessage());

            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("modalAberto", true);
            return "clientes";
        }

        return "redirect:/clientes";
    }

    @GetMapping("/titulos")
    @Transactional
    public String paginaTitulos(@RequestParam(required = false) String filtro, Model model) {
        // Agora chamamos o metodo com filtro
        List<Titulo> lista = tituloService.listarComFiltro(filtro);

        model.addAttribute("titulos", lista);
        model.addAttribute("clientes", clienteService.listarTodos());

        // Passamos o filtro de volta pra tela (pra saber o que está vendo)
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
            // Mensagem de sucesso (opcional, mas legal)
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente excluído com sucesso!");
        } catch (DataIntegrityViolationException e) {
            // AQUI É O PULO DO GATO: Se der erro de chave estrangeira (tem dívidas)
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

        // 2. O PULO DO GATO: Pegar a URL exata de onde o clique veio (com os filtros!)
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
        Titulo titulo = tituloService.buscarPorId(id); // Você precisará criar esse metodo no Service se não tiver

        model.addAttribute("titulo", titulo);
        model.addAttribute("clientes", clienteService.listarTodos()); // Precisa da lista pro Select

        return "titulo-editar"; // Vamos criar esse HTML agora
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

        return "cliente-detalhes"; // Vamos criar esse HTML agora
    }

    @GetMapping("/configuracoes")
    public String configuracoes() {
        return "configuracoes";
    }

}