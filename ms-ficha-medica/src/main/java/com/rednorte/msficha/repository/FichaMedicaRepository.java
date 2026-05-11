package com.rednorte.msficha.repository;

import com.rednorte.msficha.model.FichaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FichaMedicaRepository extends JpaRepository<FichaMedica, Long> {
    Optional<FichaMedica> findByUsuarioId(String usuarioId);
}
