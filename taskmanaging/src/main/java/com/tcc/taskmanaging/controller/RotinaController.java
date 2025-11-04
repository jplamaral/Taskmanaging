package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.service.RotinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Controller
public class RotinaController {

    @Autowired
    private RotinaService rotinaService;

    @PostMapping("/rotinas/criar")
    public String criarRotina(@ModelAttribute Rotina rotina) {
        rotinaService.criarRotina(rotina);
        return "redirect:/";
    }

    @GetMapping("/rotinas/deletar/{id}")
    public String deletarRotina(@PathVariable UUID id) {
        rotinaService.deleteRotina(id);
        return "redirect:/";
    }
}