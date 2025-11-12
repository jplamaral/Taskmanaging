package com.tcc.taskmanaging.controller;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.UsuarioRepository;
import com.tcc.taskmanaging.service.RotinaService;
import com.tcc.taskmanaging.service.TarefaService;
import com.tcc.taskmanaging.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map; // Importado para o Relatório
import java.util.Optional;


@Controller
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final TarefaService tarefaService;
    private final RotinaService rotinaService;
    private final UsuarioRepository usuarioRepository; 

    @Autowired
    public UsuarioController(UsuarioService usuarioService, TarefaService tarefaService, RotinaService rotinaService, UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.tarefaService = tarefaService;
        this.rotinaService = rotinaService;
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute
    public void addLoggedUserToModel(Model model) {
        try {
            Usuario usuario = usuarioService.getUsuarioLogado();
            model.addAttribute("usuarioLogado", usuario);
        } catch (Exception e) {
            log.trace("Nenhum usuário logado encontrado para adicionar ao modelo.");
        }
    }

    @GetMapping("/auth")
    public String showAuthPage(Model model) {
        log.info("GET /auth (página de login/cadastro)");
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "auth";
    }

    @PostMapping("/cadastro")
    public String processarCadastro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                    BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        log.info("POST /cadastro para o email: {}", usuario.getEmail());

        if (result.hasErrors()) {
            log.warn("Falha na validação do cadastro. Erros: {}", result.getAllErrors());
            model.addAttribute("showCadastro", true); 
            return "auth";
        }

        try {
            usuarioService.cadastrarUsuario(usuario);
            log.info("Cadastro de {} realizado com sucesso.", usuario.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Cadastro realizado com sucesso! Faça o login.");
            return "redirect:/auth?success";
        } catch (RuntimeException e) {
            log.warn("Falha no cadastro: {}", e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("showCadastro", true);
            return "auth";
        } catch (Exception e) {
            log.error("Erro inesperado durante o cadastro", e);
            model.addAttribute("errorMessage", "Ocorreu um erro inesperado. Tente novamente.");
            model.addAttribute("showCadastro", true);
            return "auth";
        }
    }

    @GetMapping("/")
    public String showDashboard(Model model) {
        log.info("GET / (dashboard) requisitado.");
        
        List<Tarefa> tarefas = tarefaService.getTarefasDoUsuarioLogado();
        List<Rotina> rotinas = rotinaService.getRotinasDoUsuarioLogado();

        model.addAttribute("tarefas", tarefas);
        model.addAttribute("rotinas", rotinas);
        
        if (!model.containsAttribute("novaTarefa")) {
             model.addAttribute("novaTarefa", new Tarefa());
        }
        if (!model.containsAttribute("novaRotina")) {
            model.addAttribute("novaRotina", new Rotina());
        }

        return "index";
    }

    @GetMapping("/perfil")
    public String showPerfil(Model model) {
        log.info("GET /perfil");
        // Correção para Erro 500 (NullPointerException no Thymeleaf)
        if (!model.containsAttribute("usuarioLogado")) {
             try {
                Usuario usuario = usuarioService.getUsuarioLogado();
                model.addAttribute("usuarioLogado", usuario);
            } catch (Exception e) {
                log.error("Erro ao buscar usuário logado para a página de perfil", e);
                return "redirect:/logout"; 
            }
        }
        return "perfil";
    }
    
    // Endpoint para o novo Relatório de Desempenho
    @GetMapping("/relatorio")
    public String showRelatorio(Model model) {
        log.info("GET /relatorio requisitado.");
        
        Map<String, Long> relatorioData = tarefaService.getRelatorioDesempenho();
        model.addAttribute("relatorio", relatorioData);
        
        return "relatorio"; // Renderiza relatorio.html
    }
    
    @PostMapping("/perfil/upload")
    public String uploadFoto(@RequestParam("foto") MultipartFile foto, RedirectAttributes redirectAttributes) {
        log.info("POST /perfil/upload recebido");
        try {
            Usuario usuario = usuarioService.getUsuarioLogado();
            if (usuario != null && foto != null && !foto.isEmpty()) {
                usuario.setFotoPerfil(foto.getBytes());
                usuarioService.salvar(usuario);
                log.info("Foto de perfil atualizada para usuário ID: {}", usuario.getId());
                redirectAttributes.addFlashAttribute("successMessage", "Foto atualizada com sucesso!");
            }
        } catch (Exception e) {
            log.error("Erro no upload da foto de perfil", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao salvar a foto.");
        }
        return "redirect:/perfil";
    }

    
    @GetMapping("/usuario/foto/{id}")
    public ResponseEntity<byte[]> exibirFoto(@PathVariable Long id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        byte[] imageBytes;
        MediaType mediaType = MediaType.IMAGE_PNG; 

        if (opt.isPresent() && opt.get().getFotoPerfil() != null) {
            log.debug("Exibindo foto de perfil do usuário ID: {}", id);
            imageBytes = opt.get().getFotoPerfil();
            if (imageBytes[0] == (byte)0xFF && imageBytes[1] == (byte)0xD8) {
                mediaType = MediaType.IMAGE_JPEG;
            }
        } else {
            log.debug("Usuário ID: {} sem foto ou não encontrado. Exibindo avatar padrão.", id);
            try {
                ClassPathResource imgFile = new ClassPathResource("static/img/default-avatar.png");
                try (InputStream is = imgFile.getInputStream()) {
                    imageBytes = is.readAllBytes();
                }
            } catch (IOException e) {
                log.error("Avatar padrão 'default-avatar.png' não encontrado.", e);
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mediaType.toString())
                .body(imageBytes);
    }

    @PostMapping("/usuario/atualizar")
    public String atualizarUsuario(@ModelAttribute("usuarioLogado") Usuario usuarioAtualizado,
                                   @RequestParam(value = "fotoUpload", required = false) MultipartFile fotoUpload,
                                   RedirectAttributes redirectAttributes) {
        
        log.info("POST /usuario/atualizar para usuário: {}", usuarioAtualizado.getEmail());

        try {
            usuarioService.atualizarUsuario(usuarioAtualizado, fotoUpload);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil atualizado com sucesso!");
        } catch (Exception e) {
            log.error("Erro ao atualizar perfil", e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/perfil"; 
        }
        return "redirect:/perfil";
    }
}