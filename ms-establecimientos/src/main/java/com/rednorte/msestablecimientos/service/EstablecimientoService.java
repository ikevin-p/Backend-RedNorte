package com.rednorte.msestablecimientos.service;

import com.rednorte.msestablecimientos.model.Establecimiento;
import com.rednorte.msestablecimientos.repository.EstablecimientoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EstablecimientoService {

    private final EstablecimientoRepository repository;

    public EstablecimientoService(EstablecimientoRepository repository) {
        this.repository = repository;
    }

    public List<Establecimiento> listarTodos() {
        return repository.findAll();
    }

    public List<Establecimiento> listarActivos() {
        return repository.findByEstado(Establecimiento.EstadoEstablecimiento.ACTIVO);
    }

    public List<Establecimiento> listarPorTipo(String tipo) {
        return repository.findByTipo(Establecimiento.TipoEstablecimiento.valueOf(tipo.toUpperCase()));
    }

    public List<Establecimiento> listarPorComuna(String comuna) {
        return repository.findByComunaIgnoreCase(comuna);
    }

    public List<Establecimiento> listarPorRegion(String region) {
        return repository.findByRegionIgnoreCase(region);
    }

    public Establecimiento obtenerPorId(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Establecimiento no encontrado: " + id));
    }

    public Establecimiento crear(Establecimiento establecimiento) {
        if (establecimiento.getId() == null || establecimiento.getId().isBlank()) {
            establecimiento.setId("EST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return repository.save(establecimiento);
    }

    public Establecimiento actualizar(String id, Establecimiento datos) {
        Establecimiento existente = obtenerPorId(id);

        if (datos.getNombre() != null)         existente.setNombre(datos.getNombre());
        if (datos.getTipo() != null)           existente.setTipo(datos.getTipo());
        if (datos.getDireccion() != null)      existente.setDireccion(datos.getDireccion());
        if (datos.getComuna() != null)         existente.setComuna(datos.getComuna());
        if (datos.getRegion() != null)         existente.setRegion(datos.getRegion());
        if (datos.getTelefono() != null)       existente.setTelefono(datos.getTelefono());
        if (datos.getEmail() != null)          existente.setEmail(datos.getEmail());
        if (datos.getCapacidadDiaria() != null) existente.setCapacidadDiaria(datos.getCapacidadDiaria());
        if (datos.getEstado() != null)         existente.setEstado(datos.getEstado());

        return repository.save(existente);
    }

    public void eliminar(String id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Establecimiento no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
