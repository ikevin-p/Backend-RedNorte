package com.rednorte.msestadisticas.service;

import com.rednorte.msestadisticas.dto.EstadisticasDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticasService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ms.consultas.url}")
    private String consultasUrl;

    @Value("${ms.usuarios.url}")
    private String usuariosUrl;

    @Value("${ms.agenda.url}")
    private String agendaUrl;

    @Value("${ms.establecimientos.url}")
    private String establecimientosUrl;

    public EstadisticasService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public EstadisticasDTO obtenerResumen() {
        EstadisticasDTO dto = new EstadisticasDTO();

        // Consultas
        try {
            List<Map<String, Object>> consultas = webClientBuilder.build()
                    .get().uri(consultasUrl + "/consultas")
                    .retrieve().bodyToMono(List.class).block();

            if (consultas != null) {
                dto.setTotalConsultas(consultas.size());

                Map<String, Long> porEstado = new HashMap<>();
                Map<String, Long> porDoctor = new HashMap<>();

                for (Map<String, Object> c : consultas) {
                    String estado = String.valueOf(c.getOrDefault("estado", "DESCONOCIDO"));
                    porEstado.merge(estado, 1L, Long::sum);

                    String doctor = String.valueOf(c.getOrDefault("doctorId", "SIN_ASIGNAR"));
                    porDoctor.merge(doctor, 1L, Long::sum);
                }
                dto.setConsultasPorEstado(porEstado);
                dto.setConsultasPorDoctor(porDoctor);
            }
        } catch (Exception e) {
            dto.setTotalConsultas(-1);
        }

        // Usuarios
        try {
            List<Map<String, Object>> usuarios = webClientBuilder.build()
                    .get().uri(usuariosUrl + "/usuarios")
                    .retrieve().bodyToMono(List.class).block();
            if (usuarios != null) dto.setTotalUsuarios(usuarios.size());
        } catch (Exception e) {
            dto.setTotalUsuarios(-1);
        }

        // Agenda
        try {
            List<Map<String, Object>> bloques = webClientBuilder.build()
                    .get().uri(agendaUrl + "/agenda")
                    .retrieve().bodyToMono(List.class).block();

            if (bloques != null) {
                dto.setTotalBloquesAgenda(bloques.size());

                Map<String, Long> porEstado = new HashMap<>();
                for (Map<String, Object> b : bloques) {
                    String estado = String.valueOf(b.getOrDefault("estado", "DESCONOCIDO"));
                    porEstado.merge(estado, 1L, Long::sum);
                }
                dto.setBloquesPorEstado(porEstado);
            }
        } catch (Exception e) {
            dto.setTotalBloquesAgenda(-1);
        }

        // Establecimientos
        try {
            List<Map<String, Object>> establecimientos = webClientBuilder.build()
                    .get().uri(establecimientosUrl + "/establecimientos")
                    .retrieve().bodyToMono(List.class).block();

            if (establecimientos != null) {
                dto.setTotalEstablecimientos(establecimientos.size());

                Map<String, Long> porTipo = new HashMap<>();
                for (Map<String, Object> e : establecimientos) {
                    String tipo = String.valueOf(e.getOrDefault("tipo", "DESCONOCIDO"));
                    porTipo.merge(tipo, 1L, Long::sum);
                }
                dto.setEstablecimientosPorTipo(porTipo);
            }
        } catch (Exception e) {
            dto.setTotalEstablecimientos(-1);
        }

        return dto;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> estadisticasConsultas() {
        Map<String, Object> resultado = new HashMap<>();
        try {
            List<Map<String, Object>> consultas = webClientBuilder.build()
                    .get().uri(consultasUrl + "/consultas")
                    .retrieve().bodyToMono(List.class).block();

            if (consultas != null) {
                Map<String, Long> porEstado = new HashMap<>();
                Map<String, Long> porDoctor = new HashMap<>();

                for (Map<String, Object> c : consultas) {
                    String estado = String.valueOf(c.getOrDefault("estado", "DESCONOCIDO"));
                    porEstado.merge(estado, 1L, Long::sum);

                    String doctor = String.valueOf(c.getOrDefault("doctorId", "SIN_ASIGNAR"));
                    porDoctor.merge(doctor, 1L, Long::sum);
                }
                resultado.put("total", consultas.size());
                resultado.put("porEstado", porEstado);
                resultado.put("porDoctor", porDoctor);
            }
        } catch (Exception e) {
            resultado.put("error", "No se pudo conectar con ms-consultas");
        }
        return resultado;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> estadisticasAgenda() {
        Map<String, Object> resultado = new HashMap<>();
        try {
            List<Map<String, Object>> bloques = webClientBuilder.build()
                    .get().uri(agendaUrl + "/agenda")
                    .retrieve().bodyToMono(List.class).block();

            if (bloques != null) {
                Map<String, Long> porEstado = new HashMap<>();
                Map<String, Long> porDoctor = new HashMap<>();

                for (Map<String, Object> b : bloques) {
                    String estado = String.valueOf(b.getOrDefault("estado", "DESCONOCIDO"));
                    porEstado.merge(estado, 1L, Long::sum);

                    String doctor = String.valueOf(b.getOrDefault("doctorId", "SIN_ASIGNAR"));
                    porDoctor.merge(doctor, 1L, Long::sum);
                }
                resultado.put("total", bloques.size());
                resultado.put("porEstado", porEstado);
                resultado.put("porDoctor", porDoctor);
            }
        } catch (Exception e) {
            resultado.put("error", "No se pudo conectar con ms-agenda-medica");
        }
        return resultado;
    }
}
