#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import ${package}.model.BloqueAgenda;
import ${package}.repository.BloqueAgendaRepository;
import ${package}.service.AgendaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendaServiceTest {

    @Mock
    private BloqueAgendaRepository repository;

    @InjectMocks
    private AgendaService service;

    private BloqueAgenda bloque;

    @BeforeEach
    void setUp() {
        bloque = new BloqueAgenda();
        bloque.setId(1L);
        bloque.setDoctorId("USR002");
        bloque.setEstablecimientoId("EST-001");
        bloque.setEstado(BloqueAgenda.EstadoBloque.DISPONIBLE);
    }

    @Test
    void generarBloques_creaDieciochoBloquesDe30Min() {
        when(repository.save(any(BloqueAgenda.class))).thenAnswer(i -> i.getArgument(0));
        var bloques = service.generarBloques("USR002", "EST-001", LocalDate.of(2026, 6, 20));
        assertEquals(18, bloques.size());
        verify(repository, times(18)).save(any(BloqueAgenda.class));
    }

    @Test
    void reservar_bloqueDisponible_cambiaEstadoAReservado() {
        when(repository.findById(1L)).thenReturn(Optional.of(bloque));
        when(repository.save(any(BloqueAgenda.class))).thenAnswer(i -> i.getArgument(0));
        var res = service.reservar(1L, "USR007", "5");
        assertTrue(res.isPresent());
        assertEquals(BloqueAgenda.EstadoBloque.RESERVADO, res.get().getEstado());
        assertEquals("USR007", res.get().getPacienteId());
    }

    @Test
    void reservar_bloqueNoDisponible_lanzaExcepcion() {
        bloque.setEstado(BloqueAgenda.EstadoBloque.RESERVADO);
        when(repository.findById(1L)).thenReturn(Optional.of(bloque));
        assertThrows(IllegalStateException.class, () -> service.reservar(1L, "USR007", "5"));
    }

    @Test
    void cancelar_liberaElBloque() {
        bloque.setEstado(BloqueAgenda.EstadoBloque.RESERVADO);
        bloque.setPacienteId("USR007");
        when(repository.findById(1L)).thenReturn(Optional.of(bloque));
        when(repository.save(any(BloqueAgenda.class))).thenAnswer(i -> i.getArgument(0));
        var res = service.cancelar(1L);
        assertTrue(res.isPresent());
        assertEquals(BloqueAgenda.EstadoBloque.CANCELADO, res.get().getEstado());
        assertNull(res.get().getPacienteId());
    }

    @Test
    void completar_marcaComoCompletado() {
        when(repository.findById(1L)).thenReturn(Optional.of(bloque));
        when(repository.save(any(BloqueAgenda.class))).thenAnswer(i -> i.getArgument(0));
        var res = service.completar(1L);
        assertTrue(res.isPresent());
        assertEquals(BloqueAgenda.EstadoBloque.COMPLETADO, res.get().getEstado());
    }
}
