package com.rednorte.msficha.controller;

import com.rednorte.msficha.model.FichaMedica;
import com.rednorte.msficha.service.FichaMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ficha")
@CrossOrigin(origins = "http://localhost:3000")
public class FichaMedicaController {

    @Autowired
    private FichaMedicaService service;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<FichaMedica> obtener(@PathVariable String usuarioId) {
        FichaMedica ficha = service.obtenerPorUsuario(usuarioId);
        if (ficha == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(ficha);
    }

    @PutMapping("/{usuarioId}")
    public ResponseEntity<FichaMedica> guardar(@PathVariable String usuarioId,
                                                @RequestBody FichaMedica datos) {
        return ResponseEntity.ok(service.guardar(usuarioId, datos));
    }
}
