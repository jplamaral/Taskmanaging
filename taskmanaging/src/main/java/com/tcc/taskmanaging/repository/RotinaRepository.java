package com.tcc.taskmanaging.repository;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; 

@Repository
public interface RotinaRepository extends JpaRepository<Rotina, Long> {
    
    @Query("SELECT DISTINCT r FROM Rotina r LEFT JOIN FETCH r.tarefas WHERE r.usuario = :usuario")
    List<Rotina> findByUsuario(@Param("usuario") Usuario usuario);

    
    @Query("SELECT r FROM Rotina r LEFT JOIN FETCH r.tarefas WHERE r.id = :id")
    Optional<Rotina> findByIdWithTarefas(@Param("id") Long id);
}