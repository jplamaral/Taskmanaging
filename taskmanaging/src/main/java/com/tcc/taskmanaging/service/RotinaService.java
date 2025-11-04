package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.RotinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RotinaService {

    @Autowired
    private RotinaRepository rotinaRepository;

    @Autowired
    private UsuarioService usuarioService;

    public List<Rotina> getRotinasDoUsuarioLogado() {
        Usuario usuario = usuarioService.getUsuarioLogado();
        return rotinaRepository.findByUsuario(usuario);
    }

    public void criarRotina(Rotina rotina) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        rotina.setUsuario(usuario);
        rotina.setProgresso(0);
        
        rotinaRepository.save(rotina);
    }

    public void deleteRotina(UUID id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        Rotina rotina = rotinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rotina não encontrada"));

        if (!rotina.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
            throw new RuntimeException("Acesso negado.");
        }
        
        rotinaRepository.delete(rotina);
    }

    public Rotina findById(UUID id) {
        if (id == null) {
            return null;
        }
        return rotinaRepository.findById(id).orElse(null);
    }
}