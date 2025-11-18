package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarUsuario(Usuario usuario) {
        log.info("Tentando cadastrar novo usuário: {}", usuario.getEmail());
        // Verifica no BANCO DE DADOS se o email já existe (não envia email)
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            log.warn("Falha no cadastro: Email {} já está em uso.", usuario.getEmail());
            throw new RuntimeException("Erro: Email já está em uso!");
        }

        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        log.info("Usuário cadastrado com sucesso. ID: {}", usuarioSalvo.getId());
        return usuarioSalvo;
    }

    public Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Usuário não está logado.");
        }
        
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário logado não encontrado no banco."));
    }

    public void atualizarUsuario(Usuario usuarioAtualizado, MultipartFile fotoUpload) {
        // Validação manual simples
        if (!StringUtils.hasText(usuarioAtualizado.getNome())) {
            throw new RuntimeException("O nome não pode ficar em branco.");
        }
        if (!StringUtils.hasText(usuarioAtualizado.getEmail())) {
            throw new RuntimeException("O e-mail não pode ficar em branco.");
        }

        Usuario usuarioDoBanco = getUsuarioLogado();
        log.info("Atualizando perfil do usuário ID: {}", usuarioDoBanco.getId());

        usuarioDoBanco.setNome(usuarioAtualizado.getNome());
        
        // Verifica se mudou o email e se o novo já existe no banco
        if (!usuarioDoBanco.getEmail().equals(usuarioAtualizado.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioAtualizado.getEmail())) {
                throw new RuntimeException("Email já está em uso por outra conta.");
            }
            usuarioDoBanco.setEmail(usuarioAtualizado.getEmail());
        }

        try {
            if (fotoUpload != null && !fotoUpload.isEmpty()) {
                usuarioDoBanco.setFotoPerfil(fotoUpload.getBytes());
            }
        } catch (IOException e) {
            log.error("Erro ao processar a foto de perfil", e);
            throw new RuntimeException("Erro ao processar a foto de perfil", e);
        }

        usuarioRepository.save(usuarioDoBanco);
    }
}