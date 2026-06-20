#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import ${package}.model.BloqueAgenda;
import ${package}.repository.BloqueAgendaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AgendaService {

    private final BloqueAgendaRepository repository;

    public AgendaService(BloqueAgendaRepository repository) {
        this.repository = repository;
    }

    public List<BloqueAgenda> listarTodos() {
        return repository.findAll();
    }

    public Optional<BloqueAgenda> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<BloqueAgenda> buscarPorDoctor(String doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    public List<BloqueAgenda> buscarPorDoctorYFecha(String doctorId, LocalDate fecha) {
        return repository.findByDoctorIdAndFecha(doctorId, fecha);
    }

    public List<BloqueAgenda> buscarDisponiblesPorFecha(LocalDate fecha) {
        return repository.findByFechaAndEstado(fecha, BloqueAgenda.EstadoBloque.DISPONIBLE);
    }

    public List<BloqueAgenda> buscarPorPaciente(String pacienteId) {
        return repository.findByPacienteId(pacienteId);
    }

    public BloqueAgenda crear(BloqueAgenda bloque) {
        return repository.save(bloque);
    }

    /**
     * Genera bloques automáticos cada 30 minutos para un doctor en una fecha.
     * Rango: 08:00 - 17:00
     */
    public List<BloqueAgenda> generarBloques(String doctorId, String establecimientoId, LocalDate fecha) {
        List<BloqueAgenda> bloques = new ArrayList<>();
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime fin = LocalTime.of(17, 0);

        while (inicio.isBefore(fin)) {
            BloqueAgenda bloque = new BloqueAgenda();
            bloque.setDoctorId(doctorId);
            bloque.setEstablecimientoId(establecimientoId);
            bloque.setFecha(fecha);
            bloque.setHoraInicio(inicio);
            bloque.setHoraFin(inicio.plusMinutes(30));
            bloque.setEstado(BloqueAgenda.EstadoBloque.DISPONIBLE);
            bloques.add(repository.save(bloque));
            inicio = inicio.plusMinutes(30);
        }
        return bloques;
    }

    public Optional<BloqueAgenda> reservar(Long id, String pacienteId, String consultaId) {
        return repository.findById(id).map(bloque -> {
            if (bloque.getEstado() != BloqueAgenda.EstadoBloque.DISPONIBLE) {
                throw new IllegalStateException("El bloque no está disponible");
            }
            bloque.setEstado(BloqueAgenda.EstadoBloque.RESERVADO);
            bloque.setPacienteId(pacienteId);
            bloque.setConsultaId(consultaId);
            return repository.save(bloque);
        });
    }

    public Optional<BloqueAgenda> cancelar(Long id) {
        return repository.findById(id).map(bloque -> {
            bloque.setEstado(BloqueAgenda.EstadoBloque.CANCELADO);
            bloque.setPacienteId(null);
            bloque.setConsultaId(null);
            return repository.save(bloque);
        });
    }

    public Optional<BloqueAgenda> completar(Long id) {
        return repository.findById(id).map(bloque -> {
            bloque.setEstado(BloqueAgenda.EstadoBloque.COMPLETADO);
            return repository.save(bloque);
        });
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
