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

    @Transactional
    public Cancelacion procesarSoloCancelacion(Long idBloque, String motivo) {
        BloquesAgenda bloque = bloquesRepository.findById(idBloque)
                .orElseThrow(() -> new RuntimeException("No se encontró el bloque: " + idBloque));

        Cancelacion cancelacion = new Cancelacion(bloque, LocalDateTime.now(), null, motivo);
        return cancelacionRepository.save(cancelacion);
    }

    @Transactional
    public void ejecutarReasignacion(Cancelacion cancelacion) {
        // CORRECCIÓN: apunta a ms-consultas (nuestro microservicio) en vez de ms-lista-espera
        String url = "http://cnt-ms-consultas:8083/consultas/prioritario/"
                + cancelacion.getBloque().getEspecialidadId();

        Long idNuevoPaciente;
        try {
            idNuevoPaciente = restTemplate.getForObject(url, Long.class);
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
    }
}
