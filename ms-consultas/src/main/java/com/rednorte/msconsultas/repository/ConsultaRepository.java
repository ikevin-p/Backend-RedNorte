package com.rednorte.msconsultas.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rednorte.msconsultas.model.Consulta;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    // Todas las consultas de un usuario específico
    List<Consulta> findByUsuarioId(String usuarioId);

    // Consultas por especialidad (para reasignación)
    List<Consulta> findByEspecialidadAndEstado(String especialidad, String estado);

    // Primera consulta pendiente por especialidad (para ms-reasignacion)
    Optional<Consulta> findFirstByEspecialidadAndEstadoOrderByFechaCreacionAsc(String especialidad, String estado);

    // Consultas por estado
    List<Consulta> findByEstado(String estado);
}
