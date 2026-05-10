package com.rednorte.msconsultas.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import jakarta.validation.Valid;

import com.rednorte.msconsultas.dto.ConsultaAdminDTO;
import com.rednorte.msconsultas.dto.ConsultaEditarDTO;
import com.rednorte.msconsultas.dto.ConsultaRequestDTO;
import com.rednorte.msconsultas.model.Consulta;
import com.rednorte.msconsultas.service.ConsultaService;

@RestController
@RequestMapping("/consultas")
@CrossOrigin
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    // POST /consultas — el paciente crea una nueva consulta
    @PostMapping
    public ResponseEntity<Consulta> crear(@Valid @RequestBody ConsultaRequestDTO dto) {
        return ResponseEntity.ok(consultaService.crearConsulta(dto));
    }

    // GET /consultas — el admin ve todas las consultas
    @GetMapping
    public ResponseEntity<List<Consulta>> listarTodas() {
        return ResponseEntity.ok(consultaService.listarTodas());
    }

    // GET /consultas/{id} — detalle de una consulta
    @GetMapping("/{id}")
    public ResponseEntity<Consulta> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultaService.obtenerPorId(id));
    }

    // GET /consultas/usuario/{usuarioId} — consultas del paciente logueado
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Consulta>> listarPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(consultaService.listarPorUsuario(usuarioId));
    }

    // PUT /consultas/{id}/paciente — el paciente edita nombre y síntomas
    @PutMapping("/{id}/paciente")
    public ResponseEntity<Consulta> editarPaciente(
            @PathVariable Long id,
            @RequestBody ConsultaEditarDTO dto) {
        return ResponseEntity.ok(consultaService.editarPorPaciente(id, dto));
    }

    // PUT /consultas/{id}/admin — el admin actualiza estado, fecha de cita, notas
    @PutMapping("/{id}/admin")
    public ResponseEntity<Consulta> actualizarAdmin(
            @PathVariable Long id,
            @RequestBody ConsultaAdminDTO dto) {
        return ResponseEntity.ok(consultaService.actualizarPorAdmin(id, dto));
    }

    // GET /consultas/prioritario/{especialidad} — para ms-reasignacion
    @GetMapping("/prioritario/{especialidad}")
    public ResponseEntity<Long> obtenerPrioritario(@PathVariable String especialidad) {
        Consulta c = consultaService.obtenerPrioritario(especialidad);
        return ResponseEntity.ok(c.getId());
    }

    // PUT /consultas/{id}/reasignar — ms-reasignacion confirma la reasignación
    @PutMapping("/{id}/reasignar")
    public ResponseEntity<Consulta> reasignar(
            @PathVariable Long id,
            @RequestBody Long bloquesAgendaId) {
        return ResponseEntity.ok(consultaService.marcarReasignada(id, bloquesAgendaId));
    }

    // DELETE /consultas/{id} — el admin elimina una consulta
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        consultaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Manejo de errores de validación → devuelve JSON con los campos inválidos
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensaje = error.getDefaultMessage();
            errores.put(campo, mensaje);
        });
        return errores;
    }

    // Manejo de RuntimeException (ej: consulta no encontrada)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleNotFound(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return error;
    }
}
