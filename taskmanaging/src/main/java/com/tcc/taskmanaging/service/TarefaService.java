package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final RotinaService rotinaService;

    @Autowired
    public TarefaService(TarefaRepository tarefaRepository, UsuarioService usuarioService, RotinaService rotinaService) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
        this.rotinaService = rotinaService;
    }

    public List<Tarefa> getTarefasDoUsuarioLogado() {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Buscando tarefas para o usuário ID: {}", usuario.getId());
        return tarefaRepository.findByUsuario(usuario);
    }

    public void criarTarefa(Tarefa tarefa) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        tarefa.setUsuario(usuario);
        tarefa.setStatus("pendente");

        // --- LÓGICA DE ROTINA ATUALIZADA ---
        Long idRotinaNova = tarefa.getIdRotina(); // Usa o campo transient

        if (idRotinaNova != null) {
            Optional<Rotina> rotinaOpt = rotinaService.findById(idRotinaNova);
            Rotina rotina = rotinaOpt.orElse(null);
            
            if (rotina != null && rotina.getUsuario().getId().equals(usuario.getId())) {
                tarefa.setRotina(rotina);
                log.debug("Nova tarefa associada à rotina ID: {}", rotina.getId());
            } else {
                tarefa.setRotina(null); 
                log.warn("Tentativa de associar nova tarefa à rotina ID: {} falhou.", idRotinaNova);
            }
        } else {
            tarefa.setRotina(null);
        }
        // --- FIM DA ATUALIZAÇÃO ---
        
        tarefaRepository.save(tarefa);
        log.info("Nova tarefa criada com ID: {} pelo usuário ID: {}", tarefa.getId(), usuario.getId());
    }

    public void deleteTarefa(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} tentando deletar tarefa ID: {}", usuario.getId(), id);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        tarefaRepository.delete(tarefa);
        log.info("Tarefa ID: {} deletada com sucesso.", id);
    }

    public void mudarStatusTarefa(Long id, String novoStatus) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} mudando status da tarefa ID: {} para {}", usuario.getId(), id, novoStatus);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
        log.info("Status da tarefa ID: {} atualizado.", id);
    }

    public Tarefa getTarefaById(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        return getTarefaPorIdVerificandoDono(id, usuario);
    }

    public void atualizarTarefa(Tarefa tarefaAtualizada) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} atualizando tarefa ID: {}", usuario.getId(), tarefaAtualizada.getId());
        
        Tarefa tarefaDoBanco = getTarefaPorIdVerificandoDono(tarefaAtualizada.getId(), usuario);

        tarefaDoBanco.setTitulo(tarefaAtualizada.getTitulo());
        tarefaDoBanco.setDescricao(tarefaAtualizada.getDescricao());
        tarefaDoBanco.setDataFim(tarefaAtualizada.getDataFim());
        tarefaDoBanco.setPrioridade(tarefaAtualizada.getPrioridade());

        // --- LÓGICA DE ROTINA ATUALIZADA ---
        Long idRotinaNova = tarefaAtualizada.getIdRotina(); // Usa o campo transient

        if (idRotinaNova != null) {
            Optional<Rotina> rotinaOpt = rotinaService.findById(idRotinaNova);
            Rotina rotina = rotinaOpt.orElse(null);
            
            if (rotina != null && rotina.getUsuario().getId().equals(usuario.getId())) {
                tarefaDoBanco.setRotina(rotina);
                log.debug("Tarefa ID: {} associada à rotina ID: {}", tarefaDoBanco.getId(), rotina.getId());
            } else {
                tarefaDoBanco.setRotina(null);
                log.warn("Tentativa de associar tarefa à rotina ID: {} falhou (não encontrada ou não pertence ao usuário).", idRotinaNova);
            }
        } else {
            tarefaDoBanco.setRotina(null); // Remove a associação
        }
        // --- FIM DA ATUALIZAÇÃO ---

        tarefaRepository.save(tarefaDoBanco);
        log.info("Tarefa ID: {} atualizada com sucesso.", tarefaDoBanco.getId());
    }
    
    private Tarefa getTarefaPorIdVerificandoDono(Long tarefaId, Usuario usuario) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> {
                    log.warn("Tentativa de acesso a tarefa inexistente. ID: {}", tarefaId);
                    return new RuntimeException("Tarefa não encontrada");
                });

        if (!tarefa.getUsuario().getId().equals(usuario.getId())) {
            log.warn("ACESSO NEGADO: Usuário ID: {} tentou acessar tarefa ID: {} que não lhe pertence.", usuario.getId(), tarefaId);
            throw new RuntimeException("Acesso negado: você não é o dono desta tarefa.");
        }
        
        return tarefa;
    }
}