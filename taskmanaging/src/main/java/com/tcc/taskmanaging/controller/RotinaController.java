package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.service.RotinaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RotinaController {

    private static final Logger log = LoggerFactory.getLogger(RotinaController.class);
    private final RotinaService rotinaService;

    @Autowired
    public RotinaController(RotinaService rotinaService) {
        this.rotinaService = rotinaService;
    }

    @PostMapping("/rotinas/criar")
    public String criarRotina(@ModelAttribute Rotina rotina) {
        log.info("POST /rotinas/criar chamado com nome: {}", rotina.getNome());
        try {
            rotinaService.criarRotina(rotina);
        } catch (Exception e) {
            log.error("Erro ao criar rotina", e);
            // Idealmente, aqui você adicionaria um RedirectAttributes para mostrar o erro na tela
            // Ex: redirectAttributes.addFlashAttribute("error", "Erro ao criar rotina");
        }
        return "redirect:/";
    }

    @GetMapping("/rotinas/deletar/{id}")
    public String deletarRotina(@PathVariable Long id) {
        log.info("GET /rotinas/deletar/{} chamado", id);
        try {
            rotinaService.deleteRotina(id);
        } catch (Exception e) {
            log.error("Erro ao deletar rotina ID: {}", id, e);
            // Adicionar RedirectAttributes aqui também
        }
        return "redirect:/";
    }
}