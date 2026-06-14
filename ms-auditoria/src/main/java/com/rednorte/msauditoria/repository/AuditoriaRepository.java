package com.rednorte.msauditoria.repository;

import com.rednorte.msauditoria.model.RegistroAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {

    List<RegistroAuditoria> findByUsuarioIdOrderByFechaHoraDesc(String usuarioId);

    List<RegistroAuditoria> findByModuloOrderByFechaHoraDesc(RegistroAuditoria.ModuloSistema modulo);

    List<RegistroAuditoria> findByAccionOrderByFechaHoraDesc(RegistroAuditoria.TipoAccion accion);

    List<RegistroAuditoria> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);

    List<RegistroAuditoria> findByResultadoOrderByFechaHoraDesc(RegistroAuditoria.ResultadoAccion resultado);

    List<RegistroAuditoria> findTop100ByOrderByFechaHoraDesc();
}
