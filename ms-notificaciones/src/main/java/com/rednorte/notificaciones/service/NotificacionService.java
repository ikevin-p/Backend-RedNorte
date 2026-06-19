package com.rednorte.notificaciones.service;

import com.rednorte.notificaciones.factory.NotificacionCreator;
import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.repository.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository repo;

    // Spring inyecta automaticamente TODAS las implementaciones de
    // NotificacionCreator (CambioEstadoConsultaCreator, ReasignacionCreator,
    // y cualquier nueva que se agregue en el futuro). Ver paquete factory/.
    @Autowired
    private List<NotificacionCreator> creators;

    public Notificacion crear(Notificacion n) {
        return repo.save(n);
    }

    public List<Notificacion> listarPorUsuario(String usuarioId) {
        return repo.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public List<Notificacion> listarNoLeidas(String usuarioId) {
        return repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);
    }

    public long contarNoLeidas(String usuarioId) {
        return repo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    public Notificacion marcarLeida(Long id) {
        return repo.findById(id).map(n -> {
            n.setLeida(true);
            return repo.save(n);
        }).orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
    }

    public void marcarTodasLeidas(String usuarioId) {
        List<Notificacion> noLeidas = repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        repo.saveAll(noLeidas);
    }

    /**
     * Busca el NotificacionCreator registrado para el tipo de evento dado
     * y delega en el la construccion de la notificacion (patron Factory
     * Method). Si no hay ningun creator para ese tipo, lanza una
     * excepcion clara en vez de fallar silenciosamente.
     */
    private Notificacion construirConFactory(String tipoEvento, String usuarioId, Map<String, Object> contexto) {
        return creators.stream()
                .filter(c -> c.tipoEvento().equals(tipoEvento))
                .findFirst()
                .map(c -> c.crear(usuarioId, contexto))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No hay NotificacionCreator registrado para el tipo de evento: " + tipoEvento));
    }

    // Crear notificacion cuando cambia estado de consulta
    public Notificacion notificarCambioEstado(String usuarioId, Long consultaId, String estadoAnterior, String estadoNuevo) {
        Map<String, Object> contexto = Map.of(
                "consultaId", consultaId,
                "estadoAnterior", estadoAnterior,
                "estadoNuevo", estadoNuevo
        );
        Notificacion notificacion = construirConFactory("CAMBIO_ESTADO_CONSULTA", usuarioId, contexto);
        return repo.save(notificacion);
    }

    // Crear notificacion cuando una cita cancelada es reasignada
    // automaticamente a un paciente de la lista de espera.
    public Notificacion notificarReasignacion(String usuarioId, Long consultaId, String especialidad) {
        Map<String, Object> contexto = Map.of(
                "consultaId", consultaId,
                "especialidad", especialidad
        );
        Notificacion notificacion = construirConFactory("REASIGNACION", usuarioId, contexto);
        return repo.save(notificacion);
    }
}
