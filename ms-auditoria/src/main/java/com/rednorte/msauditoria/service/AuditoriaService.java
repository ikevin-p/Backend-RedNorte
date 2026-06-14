package com.rednorte.msauditoria.service;

import com.rednorte.msauditoria.model.RegistroAuditoria;
import com.rednorte.msauditoria.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public RegistroAuditoria registrar(RegistroAuditoria registro) {
        return repository.save(registro);
    }

    public List<RegistroAuditoria> listarRecientes() {
        return repository.findTop100ByOrderByFechaHoraDesc();
    }

    public Optional<RegistroAuditoria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<RegistroAuditoria> porUsuario(String usuarioId) {
        return repository.findByUsuarioIdOrderByFechaHoraDesc(usuarioId);
    }

    public List<RegistroAuditoria> porModulo(String modulo) {
        RegistroAuditoria.ModuloSistema m = RegistroAuditoria.ModuloSistema.valueOf(modulo.toUpperCase());
        return repository.findByModuloOrderByFechaHoraDesc(m);
    }

    public List<RegistroAuditoria> porAccion(String accion) {
        RegistroAuditoria.TipoAccion a = RegistroAuditoria.TipoAccion.valueOf(accion.toUpperCase());
        return repository.findByAccionOrderByFechaHoraDesc(a);
    }

    public List<RegistroAuditoria> porRango(LocalDateTime desde, LocalDateTime hasta) {
        return repository.findByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta);
    }

    public List<RegistroAuditoria> porResultado(String resultado) {
        RegistroAuditoria.ResultadoAccion r = RegistroAuditoria.ResultadoAccion.valueOf(resultado.toUpperCase());
        return repository.findByResultadoOrderByFechaHoraDesc(r);
    }
}
