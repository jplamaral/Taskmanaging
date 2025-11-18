package com.tcc.taskmanaging.service;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import com.tcc.taskmanaging.repository.RotinaRepository;
import com.tcc.taskmanaging.repository.TarefaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final UsuarioService usuarioService;
    private final RotinaService rotinaService;
    private final RotinaRepository rotinaRepository;

    @Autowired
    public TarefaService(TarefaRepository tarefaRepository, UsuarioService usuarioService, 
                         RotinaService rotinaService, RotinaRepository rotinaRepository) {
        this.tarefaRepository = tarefaRepository;
        this.usuarioService = usuarioService;
        this.rotinaService = rotinaService;
        this.rotinaRepository = rotinaRepository;
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

        atualizarProgressoRotina(idRotinaNova);
    }

    public void deleteTarefa(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} tentando deletar tarefa ID: {}", usuario.getId(), id);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        
        Long idRotina = (tarefa.getRotina() != null) ? tarefa.getRotina().getId() : null;
        
        tarefaRepository.delete(tarefa);
        log.info("Tarefa ID: {} deletada com sucesso.", id);
        
        atualizarProgressoRotina(idRotina);
    }

    public void mudarStatusTarefa(Long id, String novoStatus) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} mudando status da tarefa ID: {} para {}", usuario.getId(), id, novoStatus);
        
        Tarefa tarefa = getTarefaPorIdVerificandoDono(id, usuario);
        tarefa.setStatus(novoStatus);

        if ("concluída".equals(novoStatus)) {
            tarefa.setDataConclusao(LocalDate.now());
        } else {
            tarefa.setDataConclusao(null);
        }

        tarefaRepository.save(tarefa);
        log.info("Status da tarefa ID: {} atualizado.", id);
        
        if (tarefa.getRotina() != null) {
            atualizarProgressoRotina(tarefa.getRotina().getId());
        }
    }

    public void atualizarTarefa(Tarefa tarefaAtualizada) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        log.info("Usuário ID: {} atualizando tarefa ID: {}", usuario.getId(), tarefaAtualizada.getId());
        
        Tarefa tarefaDoBanco = getTarefaPorIdVerificandoDono(tarefaAtualizada.getId(), usuario);
        
        Long idRotinaAntiga = (tarefaDoBanco.getRotina() != null) ? tarefaDoBanco.getRotina().getId() : null;

        tarefaDoBanco.setTitulo(tarefaAtualizada.getTitulo());
        tarefaDoBanco.setDescricao(tarefaAtualizada.getDescricao());
        tarefaDoBanco.setDataFim(tarefaAtualizada.getDataFim());
        tarefaDoBanco.setPrioridade(tarefaAtualizada.getPrioridade());

        Long idRotinaNova = tarefaAtualizada.getIdRotina(); 
        Rotina rotinaNova = null;

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
        
        atualizarProgressoRotina(idRotinaAntiga);
        
        Long idRotinaNovaSalva = (rotinaNova != null) ? rotinaNova.getId() : null;
        if (idRotinaNovaSalva != null && !idRotinaNovaSalva.equals(idRotinaAntiga)) {
            atualizarProgressoRotina(idRotinaNovaSalva);
        }
    }
    
    private void atualizarProgressoRotina(Long rotinaId) {
        if (rotinaId == null) return;

        Optional<Rotina> rotinaOpt = rotinaRepository.findByIdWithTarefas(rotinaId);
        
        if (rotinaOpt.isEmpty()) return;

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

    public Tarefa getTarefaById(Long id) {
        Usuario usuario = usuarioService.getUsuarioLogado();
        return getTarefaPorIdVerificandoDono(id, usuario);
    }

    // --- MÉTODO ATUALIZADO PARA RETORNAR LISTA E ESTATÍSTICAS (Map<String, Object>) ---
    public Map<String, Object> getRelatorioDesempenho(Integer mes, Integer ano) {
        Usuario usuario = usuarioService.getUsuarioLogado();

        LocalDate dataAtual = LocalDate.now();
        int mesBusca = (mes != null) ? mes : dataAtual.getMonthValue();
        int anoBusca = (ano != null) ? ano : dataAtual.getYear();

        LocalDate dataInicio = LocalDate.of(anoBusca, mesBusca, 1);
        LocalDate dataFim = dataInicio.withDayOfMonth(dataInicio.lengthOfMonth());

        // Busca tarefas do período
        List<Tarefa> tarefas = tarefaRepository.findByUsuarioAndPeriodo(usuario, dataInicio, dataFim);

        // Cria lista contendo APENAS as concluídas para exibir no frontend
        List<Tarefa> listaConcluidas = tarefas.stream()
            .filter(t -> "concluída".equals(t.getStatus()))
            .collect(Collectors.toList());

        // Cálculos Estatísticos
        long totalConcluidas = listaConcluidas.size();

        long concluidasNoPrazo = listaConcluidas.stream()
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

        // Prepara o Map de Retorno (Object para aceitar Long e List)
        Map<String, Object> relatorio = new HashMap<>();
        
        // Estatísticas
        relatorio.put("totalConcluidas", totalConcluidas);
        relatorio.put("concluidasNoPrazo", concluidasNoPrazo);
        relatorio.put("concluidasAtrasadas", concluidasAtrasadas);
        relatorio.put("totalPendentes", totalPendentes);
        relatorio.put("pendentesAtrasadas", pendentesAtrasadas);
        
        // Lista Detalhada
        relatorio.put("listaTarefasConcluidas", listaConcluidas);
        
        // Dados de Controle
        relatorio.put("mesSelecionado", mesBusca);
        relatorio.put("anoSelecionado", anoBusca);

        return relatorio;
    }
}