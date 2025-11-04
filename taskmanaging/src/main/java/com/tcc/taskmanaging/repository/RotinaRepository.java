package com.tcc.taskmanaging.repository;

import com.tcc.taskmanaging.model.Rotina;
import com.tcc.taskmanaging.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RotinaRepository extends JpaRepository<Rotina, UUID> {
    
    List<Rotina> findByUsuario(Usuario usuario);
}