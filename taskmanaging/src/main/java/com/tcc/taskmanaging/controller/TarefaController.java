package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.service.RotinaService;
import com.tcc.taskmanaging.service.TarefaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;

@Controller
public class TarefaController {

    @Autowired
    private TarefaService tarefaService;
    
    @Autowired
    private RotinaService rotinaService;

    @PostMapping("/tarefas/criar")
    public String criarTarefa(@ModelAttribute Tarefa tarefa) {
        tarefaService.criarTarefa(tarefa);
        return "redirect:/";
    }

    @GetMapping("/tarefas/concluir/{id}")
    public String concluirTarefa(@PathVariable UUID id) {
        tarefaService.mudarStatusTarefa(id, "concluída");
        return "redirect:/";
    }

    @GetMapping("/tarefas/deletar/{id}")
    public String deletarTarefa(@PathVariable UUID id) {
        tarefaService.deleteTarefa(id);
        return "redirect:/";
    }

    @GetMapping("/tarefas/editar/{id}")
    public String showPaginaEditar(@PathVariable UUID id, Model model) {
        Tarefa tarefa = tarefaService.getTarefaById(id);
        List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();
        
        model.addAttribute("tarefa", tarefa);
        model.addAttribute("rotinas", rotinas);
        
        return "editar-tarefa";
    }

    @PostMapping("/tarefas/editar")
    public String processarEdicao(@ModelAttribute Tarefa tarefa) {
        tarefaService.atualizarTarefa(tarefa);
        return "redirect:/";
    }
}