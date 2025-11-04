package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.service.RotinaService;
import com.tcc.taskmanaging.service.TarefaService;
import com.tcc.taskmanaging.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private RotinaService rotinaService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String showCadastroPage(Model model) {
        model.addAttribute("usuario", new Usuario()); 
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@Valid @ModelAttribute("usuario") Usuario usuario, 
                                    BindingResult result, Model model) {
        
        if (result.hasErrors()) {
            return "cadastro";
        }

        try {
            usuarioService.cadastrarUsuario(usuario);
        } catch (RuntimeException e) {
            result.rejectValue("email", null, "Este email já está em uso.");
            return "cadastro";
        }

        return "redirect:/login?success"; 
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        
        List<Tarefa> tarefas = tarefaService.getTarefasDoUsuarioLogado();
        List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();
        
        model.addAttribute("tarefas", tarefas);
        model.addAttribute("rotinas", rotinas);
        
        model.addAttribute("novaTarefa", new Tarefa());
        model.addAttribute("novaRotina", new Rotina());

        return "index";
    }
}