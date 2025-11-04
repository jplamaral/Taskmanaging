package com.tcc.taskmanaging.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id_tarefa;

    private String titulo;
    private String descricao;
    private String prioridade;
    private String status;
    private Date data_inicio;
    private Date data_fim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rotina", nullable = true)
    private Rotina rotina;

    public UUID getId_tarefa() { return id_tarefa; }
    public void setId_tarefa(UUID id_tarefa) { this.id_tarefa = id_tarefa; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getData_inicio() { return data_inicio; }
    public void setData_inicio(Date data_inicio) { this.data_inicio = data_inicio; }
    public Date getData_fim() { return data_fim; }
    public void setData_fim(Date data_fim) { this.data_fim = data_fim; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Rotina getRotina() { return rotina; }
    public void setRotina(Rotina rotina) { this.rotina = rotina; }
}