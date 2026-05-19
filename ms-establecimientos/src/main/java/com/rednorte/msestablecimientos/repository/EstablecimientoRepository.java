package com.rednorte.msestablecimientos.repository;

import com.rednorte.msestablecimientos.model.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, String> {

    List<Establecimiento> findByEstado(Establecimiento.EstadoEstablecimiento estado);

    List<Establecimiento> findByTipo(Establecimiento.TipoEstablecimiento tipo);

    List<Establecimiento> findByComunaIgnoreCase(String comuna);

    List<Establecimiento> findByRegionIgnoreCase(String region);
}
