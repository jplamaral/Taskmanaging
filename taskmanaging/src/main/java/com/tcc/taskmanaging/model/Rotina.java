package com.tcc.taskmanaging.model;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "rotinas")
public class Rotina {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id_rotina;

    private String nome;
    private String descricao;
    private String recorrencia;
    private int progresso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @OneToMany(mappedBy = "rotina", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tarefa> tarefas;

    public UUID getId_rotina() { return id_rotina; }
    public void setId_rotina(UUID id_rotina) { this.id_rotina = id_rotina; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getRecorrencia() { return recorrencia; }
    public void setRecorrencia(String recorrencia) { this.recorrencia = recorrencia; }
    public int getProgresso() { return progresso; }
    public void setProgresso(int progresso) { this.progresso = progresso; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Set<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(Set<Tarefa> tarefas) { this.tarefas = tarefas; }
}