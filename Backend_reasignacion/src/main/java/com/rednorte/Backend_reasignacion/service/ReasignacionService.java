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
    public void ejecutarReasignacionAutomatica(Long idBloque, String motivoCancelacion) {
        //Buscar el bloque de agenda usando tu atributo BloquesAgendaid
        BloquesAgenda bloque = bloquesRepository.findById(idBloque)
                .orElseThrow(() -> new RuntimeException("No se encontró el bloque: " + idBloque));

        //Registrar la Cancelacion (usando tu constructor)
        Cancelacion nuevaCancelacion = new Cancelacion(bloque, LocalDateTime.now(), null, motivoCancelacion);
        cancelacionRepository.save(nuevaCancelacion);

        //COMUNICACIÓN DOCKER: Solicitar un paciente prioritario al otro Microservicio
        // 'ms-lista-espera' es el nombre del servicio definido en tu docker-compose.yml
        String url = "http://ms-lista-espera:8081/api/lista-espera/obtener-prioritario/" + bloque.getEspecialidadId();

        Long idNuevoPaciente;
        try {
            // Intentamos obtener el ID del paciente que más necesita la hora [cite: 23, 30]
            idNuevoPaciente = restTemplate.getForObject(url, Long.class);
        } catch (Exception e) {
            // Si el otro servicio falla, el Circuit Breaker debería actuar aquí [cite: 30]
            idNuevoPaciente = null;
        }

        //Registrar en ReasignacionLog 
        boolean exito = (idNuevoPaciente != null);
        ReasignacionLog logReasignacion = new ReasignacionLog(
                nuevaCancelacion,
                exito,
                LocalDateTime.now(),
                null,
                idNuevoPaciente
        );

        reasignacionRepository.save(logReasignacion);

        // Marcamos como procesado para cumplir con tu lógica de control [cite: 23]
        nuevaCancelacion.setProcesado(true);
        cancelacionRepository.save(nuevaCancelacion);
    }
}
