package com.rednorte.bff.service;

import com.rednorte.bff.dto.DashboardDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BffService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${servicios.usuarios}")
    private String urlUsuarios;

    @Value("${servicios.consultas}")
    private String urlConsultas;

    /**
     * Construye los headers para las llamadas internas, reenviando el
     * token JWT del usuario que llamo al BFF. ms-consultas y ms-usuarios
     * ahora exigen autenticacion, por lo que sin esto las llamadas
     * fallarian con 401 (y el dashboard mostraria ceros silenciosamente).
     */
    private HttpEntity<Void> conAuth(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        if (authHeader != null && !authHeader.isBlank()) {
            headers.set("Authorization", authHeader);
        }
        return new HttpEntity<>(headers);
    }

    public DashboardDTO obtenerDashboard(String authHeader) {
        DashboardDTO dashboard = new DashboardDTO();
        HttpEntity<Void> entity = conAuth(authHeader);

        try {
            // Obtener consultas
            ResponseEntity<List<Map>> consultasResp = restTemplate.exchange(
                urlConsultas + "/consultas",
                HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map>>() {}
            );
            List<Map> consultas = consultasResp.getBody();
            if (consultas != null) {
                dashboard.setTotalConsultas(consultas.size());
                dashboard.setConsultasPendientes((int) consultas.stream()
                    .filter(c -> "PENDIENTE".equals(c.get("estado"))).count());
                dashboard.setConsultasAgendadas((int) consultas.stream()
                    .filter(c -> "AGENDADA".equals(c.get("estado"))).count());
                dashboard.setConsultasAtendidas((int) consultas.stream()
                    .filter(c -> "ATENDIDA".equals(c.get("estado"))).count());
                dashboard.setConsultasCanceladas((int) consultas.stream()
                    .filter(c -> "CANCELADA".equals(c.get("estado"))).count());
            }
        } catch (Exception e) {
            System.out.println("Error al obtener consultas: " + e.getMessage());
        }

        try {
            // Obtener usuarios
            ResponseEntity<List<Map>> usuariosResp = restTemplate.exchange(
                urlUsuarios + "/usuarios",
                HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map>>() {}
            );
            List<Map> usuarios = usuariosResp.getBody();
            if (usuarios != null) {
                dashboard.setTotalUsuarios(usuarios.size());
                dashboard.setTotalDoctores((int) usuarios.stream()
                    .filter(u -> {
                        Map rol = (Map) u.get("rol");
                        return rol != null && "DOCTOR".equals(rol.get("tag"));
                    }).count());
                dashboard.setTotalPacientes((int) usuarios.stream()
                    .filter(u -> {
                        Map rol = (Map) u.get("rol");
                        return rol != null && "PACIENTE".equals(rol.get("tag"));
                    }).count());
            }
        } catch (Exception e) {
            System.out.println("Error al obtener usuarios: " + e.getMessage());
        }

        return dashboard;
    }

    public List<Map> obtenerConsultasPaciente(String usuarioId, String authHeader) {
        try {
            ResponseEntity<List<Map>> resp = restTemplate.exchange(
                urlConsultas + "/consultas/usuario/" + usuarioId,
                HttpMethod.GET, conAuth(authHeader),
                new ParameterizedTypeReference<List<Map>>() {}
            );
            return resp.getBody();
        } catch (Exception e) {
            return List.of();
        }
    }
}
