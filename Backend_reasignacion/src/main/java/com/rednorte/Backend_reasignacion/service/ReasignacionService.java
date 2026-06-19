package com.rednorte.Backend_reasignacion.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    @Transactional
    public Cancelacion procesarSoloCancelacion(Long idBloque, String motivo) {
        BloquesAgenda bloque = bloquesRepository.findById(idBloque)
                .orElseThrow(() -> new RuntimeException("No se encontró el bloque: " + idBloque));

        Cancelacion cancelacion = new Cancelacion(bloque, LocalDateTime.now(), null, motivo);
        return cancelacionRepository.save(cancelacion);
    }

    @Transactional
    public void ejecutarReasignacion(Cancelacion cancelacion, String authHeader) {
        // CORRECCIÓN: apunta a ms-consultas (nuestro microservicio) en vez de ms-lista-espera
        String url = "http://cnt-ms-consultas:8083/consultas/prioritario/"
                + cancelacion.getBloque().getEspecialidadId();

        Long idNuevoPaciente;
        try {
            // ms-consultas ahora exige JWT en todos sus endpoints: se reenvia
            // el token del admin que disparo la reasignacion.
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null && !authHeader.isBlank()) {
                headers.set("Authorization", authHeader);
            }
            ResponseEntity<Long> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Long.class);
            idNuevoPaciente = resp.getBody();
        } catch (Exception e) {
            idNuevoPaciente = null;
        }

        boolean exito = (idNuevoPaciente != null);
        ReasignacionLog log = new ReasignacionLog(
                cancelacion, exito, LocalDateTime.now(), null, idNuevoPaciente
        );
        reasignacionRepository.save(log);

        cancelacion.setProcesado(true);
        cancelacionRepository.save(cancelacion);

        // Si la reasignacion fue exitosa, se notifica al paciente reasignado
        // (usa el Factory Method de ms-notificaciones: ReasignacionCreator).
        // Una falla al notificar no debe revertir la reasignacion misma —
        // por eso se aisla en su propio try/catch.
        if (exito) {
            notificarPacienteReasignado(idNuevoPaciente, cancelacion, authHeader);
        }
    }

    private void notificarPacienteReasignado(Long usuarioId, Cancelacion cancelacion, String authHeader) {
        try {
            String urlNotificacion = "http://cnt-ms-notificaciones:8092/notificaciones/reasignacion";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (authHeader != null && !authHeader.isBlank()) {
                headers.set("Authorization", authHeader);
            }
            java.util.Map<String, Object> body = java.util.Map.of(
                    "usuarioId", String.valueOf(usuarioId),
                    "consultaId", cancelacion.getId(),
                    "especialidad", cancelacion.getBloque().getEspecialidadId()
            );
            restTemplate.exchange(urlNotificacion, HttpMethod.POST,
                    new HttpEntity<>(body, headers), Void.class);
        } catch (Exception e) {
            // No se interrumpe la reasignacion si la notificacion falla;
            // el paciente puede ver el cambio igual al refrescar su panel.
        }
    }
}
