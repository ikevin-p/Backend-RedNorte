package com.rednorte.msconsultas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConsultaRequestDTO {

    @NotBlank(message = "El usuarioId es obligatorio")
    private String usuarioId;

    @NotBlank(message = "El nombre del paciente es obligatorio")
    private String nombrePaciente;

    @NotBlank(message = "Los síntomas son obligatorios")
    private String sintomas;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
}
