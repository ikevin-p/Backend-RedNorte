package com.rednorte.msconsultas.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO para que el administrador actualice estado, fecha de cita y notas
@Data
@NoArgsConstructor
public class ConsultaAdminDTO {
    private String estado;
    private LocalDateTime fechaCita;
    private Long bloquesAgendaId;
    private String notasAdmin;
}
