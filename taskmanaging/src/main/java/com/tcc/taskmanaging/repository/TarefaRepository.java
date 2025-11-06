package com.tcc.taskmanaging.repository;

import com.tcc.taskmanaging.model.Tarefa;
import com.tcc.taskmanaging.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @Query("SELECT t FROM Tarefa t LEFT JOIN FETCH t.rotina WHERE t.usuario = :usuario")
    List<Tarefa> findByUsuario(@Param("usuario") Usuario usuario);
}