package com.rednorte.msagendamedica.repository;

import com.rednorte.msagendamedica.model.BloqueAgenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloqueAgendaRepository extends JpaRepository<BloqueAgenda, Long> {

    List<BloqueAgenda> findByDoctorId(String doctorId);

    List<BloqueAgenda> findByDoctorIdAndFecha(String doctorId, LocalDate fecha);

    List<BloqueAgenda> findByEstablecimientoIdAndFecha(String establecimientoId, LocalDate fecha);

    List<BloqueAgenda> findByDoctorIdAndEstado(String doctorId, BloqueAgenda.EstadoBloque estado);

    List<BloqueAgenda> findByPacienteId(String pacienteId);

    List<BloqueAgenda> findByFechaAndEstado(LocalDate fecha, BloqueAgenda.EstadoBloque estado);
}
