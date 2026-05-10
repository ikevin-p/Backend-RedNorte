package com.rednorte.msconsultas.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para que el paciente edite su propia consulta (nombre y síntomas)
@Data
@NoArgsConstructor
public class ConsultaEditarDTO {
    private String nombrePaciente;
    private String sintomas;
}
