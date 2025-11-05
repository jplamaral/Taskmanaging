package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.UsuarioRepository;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TarefaService tarefaService;

    @Autowired
    private RotinaService rotinaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/auth")
    public String showAuthPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth";
    }

    @PostMapping("/cadastro")
public String processarCadastro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                BindingResult result, Model model) {

    if (result.hasErrors()) {
        model.addAttribute("errorMessage", "Verifique os campos e tente novamente.");
        model.addAttribute("showCadastro", true);
        return "auth";
    }

    try {
        usuarioService.cadastrarUsuario(usuario);
    } catch (RuntimeException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("showCadastro", true);
        return "auth";
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Ocorreu um erro inesperado ao cadastrar. Tente novamente.");
        model.addAttribute("showCadastro", true);
        return "auth";
    }

    return "redirect:/auth?success";
}

@GetMapping("/")
public String showDashboard(Model model) {
    Usuario usuario = usuarioService.getUsuarioLogado();
    model.addAttribute("usuario", usuario);

    List<Tarefa> tarefas = tarefaService.getTarefasDoUsuarioLogado();
    List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();

    model.addAttribute("tarefas", tarefas);
    model.addAttribute("rotinas", rotinas);
    model.addAttribute("novaTarefa", new Tarefa());
    model.addAttribute("novaRotina", new Rotina());

    return "index";
}

@GetMapping("/perfil")
public String showPerfil(Model model) {
    Usuario usuario = usuarioService.getUsuarioLogado();
    model.addAttribute("usuario", usuario);
    return "perfil";
}

@PostMapping("/perfil/upload")
public String uploadFoto(@RequestParam("foto") MultipartFile foto) {
    Usuario usuario = usuarioService.getUsuarioLogado();
    try {
        usuario.setFotoPerfil(foto.getBytes());
        usuarioService.salvar(usuario);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "redirect:/perfil";
}

@GetMapping("/usuario/foto/{id}")
@ResponseBody
public byte[] exibirFoto(@PathVariable UUID id) {
    return usuarioRepository.findById(id)
            .map(Usuario::getFotoPerfil)
            .orElse(null);
}

@PostMapping("/usuario/atualizar")
public String atualizarUsuario(@ModelAttribute Usuario usuario,
                               @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil) {
    usuarioService.atualizarUsuario(usuario, fotoPerfil);
    return "redirect:/perfil?sucesso";
}


}
