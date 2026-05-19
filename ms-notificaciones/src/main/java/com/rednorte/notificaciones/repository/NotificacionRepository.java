package com.rednorte.notificaciones.repository;

import com.rednorte.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(String usuarioId);
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(String usuarioId);
    long countByUsuarioIdAndLeidaFalse(String usuarioId);
}
