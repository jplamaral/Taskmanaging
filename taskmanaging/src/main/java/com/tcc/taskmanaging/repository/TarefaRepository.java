package com.tcc.taskmanaging.repository;

import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Query("SELECT t FROM Tarefa t LEFT JOIN FETCH t.rotina WHERE t.usuario = :usuario")
    List<Tarefa> findByUsuario(@Param("usuario") Usuario usuario);

    
    @Query("SELECT t FROM Tarefa t WHERE t.usuario = :usuario AND (" +
           "(t.dataConclusao IS NOT NULL AND t.dataConclusao BETWEEN :inicio AND :fim) OR " +
           "(t.dataConclusao IS NULL AND t.dataFim BETWEEN :inicio AND :fim))")
    List<Tarefa> findByUsuarioAndPeriodo(@Param("usuario") Usuario usuario, 
                                         @Param("inicio") LocalDate inicio, 
                                         @Param("fim") LocalDate fim);
}