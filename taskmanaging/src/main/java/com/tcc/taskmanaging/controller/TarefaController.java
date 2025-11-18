package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.service.RotinaService;
import com.tcc.taskmanaging.service.TarefaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam; // 1. ADICIONE ESTE IMPORT
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TarefaController {

    private static final Logger log = LoggerFactory.getLogger(TarefaController.class);
    
    private final TarefaService tarefaService;
    private final RotinaService rotinaService;


    public TarefaController(TarefaService tarefaService, RotinaService rotinaService) {
        this.tarefaService = tarefaService;
        this.rotinaService = rotinaService;
    }

    @PostMapping("/tarefas/criar")
    public String criarTarefa(@ModelAttribute("novaTarefa") Tarefa tarefa, RedirectAttributes redirectAttributes) {
        log.info("POST /tarefas/criar chamado com título: {}", tarefa.getTitulo());
        log.info("ID da Rotina recebido do formulário: {}", tarefa.getIdRotina()); 

        try {
            tarefaService.criarTarefa(tarefa);
            // 2. ADICIONE UM TOAST DE SUCESSO AQUI TAMBÉM
            redirectAttributes.addFlashAttribute("toast_message", "Tarefa criada com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao criar tarefa", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }
    
    // --- MÉTODO ATUALIZADO ---
    @GetMapping("/tarefas/concluir/{id}")
    public String concluirTarefa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("GET /tarefas/concluir/{} chamado", id);
        try {
            tarefaService.mudarStatusTarefa(id, "concluída");
            
            // 3. Adiciona atributos para o TOAST DE UNDO
            redirectAttributes.addFlashAttribute("toast_message", "Tarefa marcada como concluída.");
            redirectAttributes.addFlashAttribute("undo_action", "concluir"); // O que foi feito
            redirectAttributes.addFlashAttribute("undo_tarefa_id", id); // Qual tarefa
            
        } catch (Exception e) {
            log.error("Erro ao concluir tarefa ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao concluir tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }

    // --- MÉTODO ATUALIZADO ---
    @GetMapping("/tarefas/deletar/{id}")
    public String deletarTarefa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("GET /tarefas/deletar/{} chamado", id);
        try {
            tarefaService.deleteTarefa(id);
            
            // 4. Adiciona atributo para um TOAST SIMPLES (sem undo)
            redirectAttributes.addFlashAttribute("toast_message", "Tarefa excluída com sucesso.");
            // Sem "undo_action", o botão "Desfazer" não aparecerá
            
        } catch (Exception e) {
            log.error("Erro ao deletar tarefa ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/tarefas/editar/{id}")
    public String showPaginaEditar(@PathVariable Long id, Model model) {
        // ... (Este método está correto, sem mudanças)
        log.info("GET /tarefas/editar/{} chamado", id);
        try {
            Tarefa tarefa = tarefaService.getTarefaById(id);
            List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();
            
            if (tarefa.getRotina() != null) {
                tarefa.setIdRotina(tarefa.getRotina().getId());
            }

            model.addAttribute("tarefa", tarefa);
            model.addAttribute("rotinas", rotinas);
            
            return "editar-tarefa";
        } catch (Exception e) {
            log.error("Erro ao carregar página de edição para tarefa ID: {}", id, e);
            return "redirect:/"; 
        }
    }

    @PostMapping("/tarefas/editar")
    public String processarEdicao(@ModelAttribute("tarefa") Tarefa tarefa, RedirectAttributes redirectAttributes) {
        log.info("POST /tarefas/editar chamado para tarefa ID: {}", tarefa.getId());
        try {
            tarefaService.atualizarTarefa(tarefa);
            // 5. ADICIONE UM TOAST DE SUCESSO AQUI TAMBÉM
            redirectAttributes.addFlashAttribute("toast_message", "Tarefa atualizada com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao atualizar tarefa ID: {}", tarefa.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }
    

    @PostMapping("/tarefas/undo")
    public String undoTarefa(@RequestParam("action") String action, 
                             @RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes) {
        
        log.info("POST /tarefas/undo chamado para action: {} no ID: {}", action, id);
        try {
            if ("concluir".equals(action)) {
                // Reverte a ação "concluir"
                tarefaService.mudarStatusTarefa(id, "pendente"); 
                redirectAttributes.addFlashAttribute("toast_message", "Ação desfeita.");
            }
            // (Futuramente, você poderia adicionar um case "deletar" aqui)
            
        } catch (Exception e) {
            log.error("Erro ao desfazer ação: {}", action, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao desfazer ação.");
        }
        return "redirect:/";
    }
}