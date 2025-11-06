package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.RotinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RotinaService {

    private static final Logger log = LoggerFactory.getLogger(RotinaService.class);

    private final RotinaRepository rotinaRepository;
    private final UsuarioService usuarioService;

    @Autowired
    public RotinaService(RotinaRepository rotinaRepository, UsuarioService usuarioService) {
        this.rotinaRepository = rotinaRepository;
        this.usuarioService = usuarioService;
    }

    public List<Rotina> getRotinasDoUsuarioLogado() {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Buscando rotinas para o usuário ID: {}", usuario.getId());
        return rotinaRepository.findByUsuario(usuario);
    }

    public void criarRotina(Rotina rotina) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        rotina.setUsuario(usuario);
        rotina.setProgresso(0);
        
        rotinaRepository.save(rotina);
        log.info("Nova rotina criada com ID: {} pelo usuário ID: {}", rotina.getId(), usuario.getId());
    }

    public void deleteRotina(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} tentando deletar rotina ID: {}", usuario.getId(), id);

        Rotina rotina = rotinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rotina não encontrada com ID: " + id));

        if (!rotina.getUsuario().getId().equals(usuario.getId())) {
            log.warn("ACESSO NEGADO: Usuário ID: {} tentou deletar rotina ID: {} que não lhe pertence.", usuario.getId(), id);
            throw new RuntimeException("Acesso negado.");
        }
        
        rotinaRepository.delete(rotina);
        log.info("Rotina ID: {} deletada com sucesso.", id);
    }

    public Optional<Rotina> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return rotinaRepository.findById(id);
    }
}