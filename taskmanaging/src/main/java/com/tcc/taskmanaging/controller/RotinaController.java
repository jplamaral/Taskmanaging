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
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Importar

@Controller
public class RotinaController {

    private static final Logger log = LoggerFactory.getLogger(RotinaController.class);
    private final RotinaService rotinaService;

    @Autowired
    public RotinaController(RotinaService rotinaService) {
        this.rotinaService = rotinaService;
    }

    // --- CORREÇÃO APLICADA AQUI ---
    @PostMapping("/rotinas/criar")
    public String criarRotina(@ModelAttribute("novaRotina") Rotina rotina, RedirectAttributes redirectAttributes) {
        log.info("POST /rotinas/criar chamado com nome: {}", rotina.getNome());
        try {
            rotinaService.criarRotina(rotina);
        } catch (Exception e) {
            log.error("Erro ao criar rotina", e);
            // Adiciona feedback de erro
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao criar rotina: " + e.getMessage());
        }
        return "redirect:/";
    }

    // Apenas adicionei o RedirectAttributes para feedback
    @GetMapping("/rotinas/deletar/{id}")
    public String deletarRotina(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("GET /rotinas/deletar/{} chamado", id);
        try {
            rotinaService.deleteRotina(id);
        } catch (Exception e) {
            log.error("Erro ao deletar rotina ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao deletar rotina: " + e.getMessage());
        }
        return "redirect:/";
    }
}