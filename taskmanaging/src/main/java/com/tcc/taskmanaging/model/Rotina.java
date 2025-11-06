package com.tcc.taskmanaging.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "rotinas")
public class Rotina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rotina") // Mapeia para sua coluna existente
    private Long id;

    @NotBlank(message = "O nome da rotina é obrigatório.")
    @Size(max = 100)
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Size(max = 255)
    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "recorrencia", length = 50)
    private String recorrencia;

    @Column(name = "progresso", nullable = false)
    private int progresso = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "rotina", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Tarefa> tarefas;

    // --- Construtores ---
    public Rotina() {}

    public Rotina(String nome, String descricao, String recorrencia, Usuario usuario) {
        this.nome = nome;
        this.descricao = descricao;
        this.recorrencia = recorrencia;
        this.usuario = usuario;
    }

    // --- Getters e Setters (Padronizados para 'id') ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRecorrencia() {
        return recorrencia;
    }

    public void setRecorrencia(String recorrencia) {
        this.recorrencia = recorrencia;
    }

    public int getProgresso() {
        return progresso;
    }

    public void setProgresso(int progresso) {
        this.progresso = progresso;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Set<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(Set<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }
}