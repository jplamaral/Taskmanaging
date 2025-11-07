package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.RotinaRepository; // 1. ADICIONE ESTE IMPORT
import com.tcc.taskmanaging.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set; // 2. ADICIONE ESTE IMPORT

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final RotinaService rotinaService;
    private final RotinaRepository rotinaRepository; // 3. ADICIONE ESTE REPOSITÓRIO

    @Autowired
    public TarefaService(TarefaRepository tarefaRepository, UsuarioService usuarioService, 
                         RotinaService rotinaService, RotinaRepository rotinaRepository) { // 4. ADICIONE AO CONSTRUTOR
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
        this.rotinaService = rotinaService;
        this.rotinaRepository = rotinaRepository; // 5. INICIALIZE AQUI
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

        Long idRotinaNova = tarefa.getIdRotina(); 

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
        
        tarefaRepository.save(tarefa);
        log.info("Nova tarefa criada com ID: {} pelo usuário ID: {}", tarefa.getId(), usuario.getId());

        // 6. Chame o método de atualização do progresso
        atualizarProgressoRotina(idRotinaNova);
    }

    public void deleteTarefa(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} tentando deletar tarefa ID: {}", usuario.getId(), id);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        
        // 7. Pega o ID da rotina ANTES de deletar
        Long idRotina = (tarefa.getRotina() != null) ? tarefa.getRotina().getId() : null;
        
        tarefaRepository.delete(tarefa);
        log.info("Tarefa ID: {} deletada com sucesso.", id);
        
        // 8. Atualiza a rotina (se ela existia)
        atualizarProgressoRotina(idRotina);
    }

    public void mudarStatusTarefa(Long id, String novoStatus) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} mudando status da tarefa ID: {} para {}", usuario.getId(), id, novoStatus);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        tarefa.setStatus(novoStatus);
        tarefaRepository.save(tarefa);
        log.info("Status da tarefa ID: {} atualizado.", id);
        
        // 9. Atualiza o progresso da rotina
        if (tarefa.getRotina() != null) {
            atualizarProgressoRotina(tarefa.getRotina().getId());
        }
    }

    public void atualizarTarefa(Tarefa tarefaAtualizada) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} atualizando tarefa ID: {}", usuario.getId(), tarefaAtualizada.getId());
        
        Tarefa tarefaDoBanco = getTarefaPorIdVerificandoDono(tarefaAtualizada.getId(), usuario);
        
        // 10. Pega o ID da rotina antiga ANTES de salvar
        Long idRotinaAntiga = (tarefaDoBanco.getRotina() != null) ? tarefaDoBanco.getRotina().getId() : null;

        tarefaDoBanco.setTitulo(tarefaAtualizada.getTitulo());
        tarefaDoBanco.setDescricao(tarefaAtualizada.getDescricao());
        tarefaDoBanco.setDataFim(tarefaAtualizada.getDataFim());
        tarefaDoBanco.setPrioridade(tarefaAtualizada.getPrioridade());

        Long idRotinaNova = tarefaAtualizada.getIdRotina(); 
        Rotina rotinaNova = null; // Guarda a nova rotina

        if (idRotinaNova != null) {
            Optional<Rotina> rotinaOpt = rotinaService.findById(idRotinaNova);
            rotinaNova = rotinaOpt.orElse(null);
            
            if (rotinaNova != null && rotinaNova.getUsuario().getId().equals(usuario.getId())) {
                tarefaDoBanco.setRotina(rotinaNova);
                log.debug("Tarefa ID: {} associada à rotina ID: {}", tarefaDoBanco.getId(), rotinaNova.getId());
            } else {
                tarefaDoBanco.setRotina(null);
                log.warn("Tentativa de associar tarefa à rotina ID: {} falhou.", idRotinaNova);
            }
        } else {
            tarefaDoBanco.setRotina(null); 
        }

        tarefaRepository.save(tarefaDoBanco);
        log.info("Tarefa ID: {} atualizada com sucesso.", tarefaDoBanco.getId());
        
        // 11. Atualiza o progresso das rotinas
        // Atualiza a rotina antiga (de onde a tarefa pode ter saído)
        atualizarProgressoRotina(idRotinaAntiga);
        
        // Se a nova rotina for diferente da antiga, atualiza ela também
        Long idRotinaNovaSalva = (rotinaNova != null) ? rotinaNova.getId() : null;
        if (idRotinaNovaSalva != null && !idRotinaNovaSalva.equals(idRotinaAntiga)) {
            atualizarProgressoRotina(idRotinaNovaSalva);
        }
    }
    
    // 12. ADICIONE ESTE NOVO MÉTODO HELPER
    /**
     * Calcula e salva o percentual de progresso de uma rotina.
     */
    private void atualizarProgressoRotina(Long rotinaId) {
        if (rotinaId == null) {
            return; // Tarefa avulsa, não faz nada
        }

        // Usamos o novo método do repositório para garantir que as tarefas venham
        Optional<Rotina> rotinaOpt = rotinaRepository.findByIdWithTarefas(rotinaId);
        
        if (rotinaOpt.isEmpty()) {
            log.warn("Não foi possível encontrar a rotina ID {} para atualizar o progresso.", rotinaId);
            return;
        }

        Rotina rotina = rotinaOpt.get();
        Set<Tarefa> tarefas = rotina.getTarefas();

        if (tarefas == null || tarefas.isEmpty()) {
            rotina.setProgresso(0);
        } else {
            double concluidas = tarefas.stream()
                                      .filter(t -> "concluída".equals(t.getStatus()))
                                      .count();
            
            double total = tarefas.size();
            int progresso = (int) Math.round((concluidas / total) * 100);
            rotina.setProgresso(progresso);
        }
        
        rotinaRepository.save(rotina);
        log.info("Progresso da rotina ID {} atualizado para {}%", rotina.getId(), rotina.getProgresso());
    }
    
        // ... mantém tudo o que já existe acima

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

    
    public Tarefa getTarefaById(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada com ID: " + id));
    }
}
