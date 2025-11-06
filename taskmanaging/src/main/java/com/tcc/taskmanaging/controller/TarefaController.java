package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.service.RotinaService;
import com.tcc.taskmanaging.service.TarefaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TarefaController {

    private static final Logger log = LoggerFactory.getLogger(TarefaController.class);
    
    private final TarefaService tarefaService;
    private final RotinaService rotinaService;

    @Autowired
    public TarefaController(TarefaService tarefaService, RotinaService rotinaService) {
        this.tarefaService = tarefaService;
        this.rotinaService = rotinaService;
    }

    @PostMapping("/tarefas/criar")
    public String criarTarefa(@ModelAttribute Tarefa tarefa, RedirectAttributes redirectAttributes) {
        log.info("POST /tarefas/criar chamado com título: {}", tarefa.getTitulo());
        try {
            tarefaService.criarTarefa(tarefa);
        } catch (Exception e) {
            log.error("Erro ao criar tarefa", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/tarefas/concluir/{id}")
    public String concluirTarefa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("GET /tarefas/concluir/{} chamado", id);
        try {
            tarefaService.mudarStatusTarefa(id, "concluída");
        } catch (Exception e) {
            log.error("Erro ao concluir tarefa ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao concluir tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/tarefas/deletar/{id}")
    public String deletarTarefa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("GET /tarefas/deletar/{} chamado", id);
        try {
            tarefaService.deleteTarefa(id);
        } catch (Exception e) {
            log.error("Erro ao deletar tarefa ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/tarefas/editar/{id}")
    public String showPaginaEditar(@PathVariable Long id, Model model) {
        log.info("GET /tarefas/editar/{} chamado", id);
        try {
            Tarefa tarefa = tarefaService.getTarefaById(id);
            List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();
            
            // Popula o campo transient para o formulário
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
    public String processarEdicao(@ModelAttribute Tarefa tarefa, RedirectAttributes redirectAttributes) {
        log.info("POST /tarefas/editar chamado para tarefa ID: {}", tarefa.getId());
        try {
            tarefaService.atualizarTarefa(tarefa);
        } catch (Exception e) {
            log.error("Erro ao atualizar tarefa ID: {}", tarefa.getId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao atualizar tarefa: " + e.getMessage());
        }
        return "redirect:/";
    }
}