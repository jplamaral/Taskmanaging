package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.RotinaRepository; // Importado
import com.tcc.taskmanaging.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate; // Importado
import java.util.HashMap; // Importado
import java.util.List;
import java.util.Map; // Importado
import java.util.Optional;
import java.util.Set; 

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final RotinaService rotinaService;
    private final RotinaRepository rotinaRepository; // Adicionado

    @Autowired
    public TarefaService(TarefaRepository tarefaRepository, UsuarioService usuarioService, 
                         RotinaService rotinaService, RotinaRepository rotinaRepository) { // Adicionado
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
        this.rotinaService = rotinaService;
        this.rotinaRepository = rotinaRepository; // Adicionado
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

        // Chama o método de atualização do progresso
        atualizarProgressoRotina(idRotinaNova);
    }

    public void deleteTarefa(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} tentando deletar tarefa ID: {}", usuario.getId(), id);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        
        // Pega o ID da rotina ANTES de deletar
        Long idRotina = (tarefa.getRotina() != null) ? tarefa.getRotina().getId() : null;
        
        tarefaRepository.delete(tarefa);
        log.info("Tarefa ID: {} deletada com sucesso.", id);
        
        // Atualiza a rotina (se ela existia)
        atualizarProgressoRotina(idRotina);
    }

    public void mudarStatusTarefa(Long id, String novoStatus) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} mudando status da tarefa ID: {} para {}", usuario.getId(), id, novoStatus);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        tarefa.setStatus(novoStatus);

        // Lógica de Data de Conclusão (para o Relatório)
        if ("concluída".equals(novoStatus)) {
            // Se está concluindo, define a data de hoje
            tarefa.setDataConclusao(LocalDate.now());
        } else {
            // Se está revertendo para "pendente" (ex: via "Desfazer"), limpa a data
            tarefa.setDataConclusao(null);
        }

        tarefaRepository.save(tarefa);
        log.info("Status da tarefa ID: {} atualizado.", id);
        
        // Atualiza o progresso da rotina
        if (tarefa.getRotina() != null) {
            atualizarProgressoRotina(tarefa.getRotina().getId());
        }
    }

    public void atualizarTarefa(Tarefa tarefaAtualizada) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} atualizando tarefa ID: {}", usuario.getId(), tarefaAtualizada.getId());
        
        Tarefa tarefaDoBanco = getTarefaPorIdVerificandoDono(tarefaAtualizada.getId(), usuario);
        
        // Pega o ID da rotina antiga ANTES de salvar
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
        
        // Atualiza o progresso das rotinas
        atualizarProgressoRotina(idRotinaAntiga);
        
        Long idRotinaNovaSalva = (rotinaNova != null) ? rotinaNova.getId() : null;
        if (idRotinaNovaSalva != null && !idRotinaNovaSalva.equals(idRotinaAntiga)) {
            atualizarProgressoRotina(idRotinaNovaSalva);
        }
    }
    
    /**
     * Calcula e salva o percentual de progresso de uma rotina.
     */
    private void atualizarProgressoRotina(Long rotinaId) {
        if (rotinaId == null) {
            return; // Tarefa avulsa, não faz nada
        }

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
    
    /**
     * Método de segurança interno.
     */
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

    /**
     * Método público usado pelo TarefaController para carregar a página de edição.
     */
    public Tarefa getTarefaById(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        return getTarefaPorIdVerificandoDono(id, usuario);
    }

    /**
     * Gera um relatório de desempenho de tarefas para o usuário logado.
     * @return Um Map com as estatísticas.
     */
    public Map<String, Long> getRelatorioDesempenho() {
        Usuario usuario = usuarioService.getUsuarioLogado();
        List<Tarefa> tarefas = tarefaRepository.findByUsuario(usuario);

        long totalConcluidas = tarefas.stream()
            .filter(t -> "concluída".equals(t.getStatus()))
            .count();

        long concluidasNoPrazo = tarefas.stream()
            .filter(t -> t.getDataConclusao() != null && 
                         t.getDataFim() != null && 
                         !t.getDataConclusao().isAfter(t.getDataFim()))
            .count();

        long concluidasAtrasadas = totalConcluidas - concluidasNoPrazo;

        long totalPendentes = tarefas.size() - totalConcluidas;

        long pendentesAtrasadas = tarefas.stream()
            .filter(t -> "pendente".equals(t.getStatus()) &&
                         t.getDataFim() != null &&
                         t.getDataFim().isBefore(LocalDate.now()))
            .count();

        Map<String, Long> relatorio = new HashMap<>();
        relatorio.put("totalConcluidas", totalConcluidas);
        relatorio.put("concluidasNoPrazo", concluidasNoPrazo);
        relatorio.put("concluidasAtrasadas", concluidasAtrasadas);
        relatorio.put("totalPendentes", totalPendentes);
        relatorio.put("pendentesAtrasadas", pendentesAtrasadas);

        return relatorio;
    }
}