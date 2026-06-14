package com.rednorte.msestablecimientos;

import com.rednorte.msestablecimientos.model.Establecimiento;
import com.rednorte.msestablecimientos.repository.EstablecimientoRepository;
import com.rednorte.msestablecimientos.service.EstablecimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstablecimientoServiceTest {

    @Mock
    private EstablecimientoRepository repository;

    @InjectMocks
    private EstablecimientoService service;

    private Establecimiento est;

    @BeforeEach
    void setUp() {
        est = new Establecimiento();
        est.setId("EST-001");
        est.setNombre("Hospital Norte");
        est.setTipo(Establecimiento.TipoEstablecimiento.HOSPITAL);
        est.setDireccion("Av. Prat 1234");
        est.setComuna("Iquique");
        est.setRegion("Tarapaca");
        est.setEstado(Establecimiento.EstadoEstablecimiento.ACTIVO);
    }

    @Test
    void crear_sinId_generaIdAutomatico() {
        Establecimiento nuevo = new Establecimiento();
        nuevo.setNombre("CESFAM Centro");
        when(repository.save(any(Establecimiento.class))).thenAnswer(i -> i.getArgument(0));
        Establecimiento res = service.crear(nuevo);
        assertNotNull(res.getId());
        assertTrue(res.getId().startsWith("EST-"));
    }

    @Test
    void crear_conId_conservaElId() {
        when(repository.save(any(Establecimiento.class))).thenAnswer(i -> i.getArgument(0));
        Establecimiento res = service.crear(est);
        assertEquals("EST-001", res.getId());
    }

    @Test
    void obtenerPorId_existente_retornaEstablecimiento() {
        when(repository.findById("EST-001")).thenReturn(Optional.of(est));
        Establecimiento res = service.obtenerPorId("EST-001");
        assertEquals("Hospital Norte", res.getNombre());
    }

    @Test
    void obtenerPorId_inexistente_lanzaExcepcion() {
        when(repository.findById("EST-999")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.obtenerPorId("EST-999"));
    }

    @Test
    void actualizar_modificaSoloCamposNoNulos() {
        Establecimiento cambios = new Establecimiento();
        cambios.setNombre("Hospital Regional Norte");
        when(repository.findById("EST-001")).thenReturn(Optional.of(est));
        when(repository.save(any(Establecimiento.class))).thenAnswer(i -> i.getArgument(0));
        Establecimiento res = service.actualizar("EST-001", cambios);
        assertEquals("Hospital Regional Norte", res.getNombre());
        assertEquals("Iquique", res.getComuna());
    }

    @Test
    void eliminar_inexistente_lanzaExcepcion() {
        when(repository.existsById("EST-999")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.eliminar("EST-999"));
    }
}
