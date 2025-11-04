package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.TarefaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TarefaService {

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RotinaService rotinaService;

    public List<Tarefa> getTarefasDoUsuarioLogado() {
        Usuario usuario = usuarioService.getUsuarioLogado();
        return tarefaRepository.findByUsuario(usuario);
    }

    public void criarTarefa(Tarefa tarefa) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        tarefa.setUsuario(usuario);
        tarefa.setStatus("pendente");

        if (tarefa.getRotina() != null && tarefa.getRotina().getId_rotina() != null) {
            Rotina rotina = rotinaService.findById(tarefa.getRotina().getId_rotina());
            
            if (rotina != null && rotina.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
                tarefa.setRotina(rotina);
            } else {
                tarefa.setRotina(null);
            }
        } else {
            tarefa.setRotina(null);
        }
        
        tarefaRepository.save(tarefa);
    }

    public void deleteTarefa(UUID id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
            throw new RuntimeException("Acesso negado: Você não é o dono desta tarefa.");
        }
        
        tarefaRepository.delete(tarefa);
    }

    public void mudarStatusTarefa(UUID id, String novoStatus) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        
        if (!tarefa.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
            throw new RuntimeException("Acesso negado: Você não é o dono desta tarefa.");
        }

        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
    }

    public Tarefa getTarefaById(UUID id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        Tarefa tarefa = tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (!tarefa.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
            throw new RuntimeException("Acesso negado.");
        }
        return tarefa;
    }

    public void atualizarTarefa(Tarefa tarefaAtualizada) {
        Tarefa tarefaDoBanco = getTarefaById(tarefaAtualizada.getId_tarefa());
        Usuario usuario = usuarioService.getUsuarioLogado();

        tarefaDoBanco.setTitulo(tarefaAtualizada.getTitulo());
        tarefaDoBanco.setDescricao(tarefaAtualizada.getDescricao());
        tarefaDoBanco.setData_fim(tarefaAtualizada.getData_fim());
        tarefaDoBanco.setPrioridade(tarefaAtualizada.getPrioridade());

        if (tarefaAtualizada.getRotina() != null && tarefaAtualizada.getRotina().getId_rotina() != null) {
            Rotina rotina = rotinaService.findById(tarefaAtualizada.getRotina().getId_rotina());
            if (rotina != null && rotina.getUsuario().getId_usuario().equals(usuario.getId_usuario())) {
                tarefaDoBanco.setRotina(rotina);
            } else {
                tarefaDoBanco.setRotina(null);
            }
        } else {
            tarefaDoBanco.setRotina(null);
        }

        tarefaRepository.save(tarefaDoBanco);
    }
}