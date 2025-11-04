package com.tcc.taskmanaging.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id_usuario;

    @NotBlank
    @Size(max = 100)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(max = 255)
    private String senha;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Tarefa> tarefas;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Rotina> rotinas;

    public UUID getId_usuario() { return id_usuario; }
    public void setId_usuario(UUID id_usuario) { this.id_usuario = id_usuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Set<Tarefa> getTarefas() { return tarefas; }
    public void setTarefas(Set<Tarefa> tarefas) { this.tarefas = tarefas; }
    public Set<Rotina> getRotinas() { return rotinas; }
    public void setRotinas(Set<Rotina> rotinas) { this.rotinas = rotinas; }
}