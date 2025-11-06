package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils; // 1. Adicione este import

import java.io.IOException;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario cadastrarUsuario(Usuario usuario) {
        // ... (sem mudanças)
        log.info("Tentando cadastrar novo usuário com email: {}", usuario.getEmail());
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
        // ... (sem mudanças)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Tentativa de acesso sem autenticação.");
            throw new RuntimeException("Usuário não está logado.");
        }
        
        String email = authentication.getName();
        log.debug("Buscando usuário logado com email: {}", email);

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Usuário autenticado ({}) não encontrado no banco de dados.", email);
                    return new RuntimeException("Usuário logado não encontrado no banco.");
                });
    }

    // --- CORREÇÃO APLICADA AQUI ---
    // 1. Renomeado o parâmetro para "fotoUpload"
    public void atualizarUsuario(Usuario usuarioAtualizado, MultipartFile fotoUpload) {

        // 2. Adicionada validação manual
        if (!StringUtils.hasText(usuarioAtualizado.getNome())) {
            throw new RuntimeException("O nome não pode ficar em branco.");
        }
        if (!StringUtils.hasText(usuarioAtualizado.getEmail())) {
            throw new RuntimeException("O e-mail não pode ficar em branco.");
        }
        // --- Fim da validação ---

        Usuario usuario = getUsuarioLogado();
        log.info("Atualizando perfil do usuário ID: {}", usuario.getId());

        usuario.setNome(usuarioAtualizado.getNome());
        
        if (!usuario.getEmail().equals(usuarioAtualizado.getEmail())) {
            if (usuarioRepository.existsByEmail(usuarioAtualizado.getEmail())) {
                log.warn("Falha na atualização: Email {} já pertence a outro usuário.", usuarioAtualizado.getEmail());
                throw new RuntimeException("Email já está em uso por outra conta.");
            }
            usuario.setEmail(usuarioAtualizado.getEmail());
        }

        try {
            // 3. Usando "fotoUpload" aqui
            if (fotoUpload != null && !fotoUpload.isEmpty()) {
                usuario.setFotoPerfil(fotoUpload.getBytes());
                log.info("Nova foto de perfil salva para o usuário ID: {}", usuario.getId());
            }
        } catch (IOException e) {
            log.error("Erro ao processar a foto de perfil para o usuário ID: {}", usuario.getId(), e);
            throw new RuntimeException("Erro ao processar a foto de perfil", e);
        }

        usuarioRepository.save(usuario);
        log.info("Perfil do usuário ID: {} atualizado com sucesso.", usuario.getId());
    }

    public void salvar(Usuario usuario) {
        usuarioRepository.save(usuario);
    }
}