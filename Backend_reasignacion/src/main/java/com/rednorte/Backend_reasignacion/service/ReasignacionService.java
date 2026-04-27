package com.rednorte.Backend_reasignacion.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.rednorte.Backend_reasignacion.model.BloquesAgenda;
import com.rednorte.Backend_reasignacion.model.Cancelacion;
import com.rednorte.Backend_reasignacion.model.ReasignacionLog;
import com.rednorte.Backend_reasignacion.repository.BloquesAgendaRepository;
import com.rednorte.Backend_reasignacion.repository.CancelacionRepository;
import com.rednorte.Backend_reasignacion.repository.ReasignacionRepository;

@Service
public class ReasignacionService {

    @Autowired
    private BloquesAgendaRepository bloquesRepository;
    @Autowired
    private CancelacionRepository cancelacionRepository;
    @Autowired
    private ReasignacionRepository reasignacionRepository;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * SOLICITUD 1: Procesa estrictamente la cancelación de la cita.
     */
    @Transactional
    public Cancelacion procesarSoloCancelacion(Long idBloque, String motivo) {
        BloquesAgenda bloque = bloquesRepository.findById(idBloque)
                .orElseThrow(() -> new RuntimeException("No se encontró el bloque: " + idBloque));

        // Registrar la cancelación en la BD
        Cancelacion cancelacion = new Cancelacion(bloque, LocalDateTime.now(), null, motivo);
        return cancelacionRepository.save(cancelacion);
    }

    /**
     * SOLICITUD 2: Procesa la reasignación buscando un nuevo paciente via
     * Docker.
     */
    @Transactional
    public void ejecutarReasignacion(Cancelacion cancelacion) {
        // COMUNICACIÓN DOCKER: 'ms-lista-espera' es el nombre del servicio en docker-compose
        String url = "http://ms-lista-espera:8081/api/lista-espera/obtener-prioritario/"
                + cancelacion.getBloque().getEspecialidadId();

        Long idNuevoPaciente;
        try {
            // Llamada al microservicio externo
            idNuevoPaciente = restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            idNuevoPaciente = null;
        }

        // Registrar el resultado en el Log de auditoría
        boolean exito = (idNuevoPaciente != null);
        ReasignacionLog log = new ReasignacionLog(
                cancelacion, exito, LocalDateTime.now(), null, idNuevoPaciente
        );
        reasignacionRepository.save(log);

        // Marcar la cancelación como procesada tras el intento de reasignación
        cancelacion.setProcesado(true);
        cancelacionRepository.save(cancelacion);
    }
}
