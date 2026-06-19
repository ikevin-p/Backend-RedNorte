package com.rednorte.msficha.service;

import com.rednorte.msficha.model.FichaMedica;
import com.rednorte.msficha.repository.FichaMedicaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("Tests unitarios — FichaMedicaService")
class FichaMedicaServiceTest {

    @Mock
    private FichaMedicaRepository repo;

    @InjectMocks
    private FichaMedicaService service;

    private FichaMedica fichaExistente;

    @BeforeEach
    void setUp() {
        fichaExistente = new FichaMedica();
        fichaExistente.setId(1L);
        fichaExistente.setUsuarioId("USR001");
        fichaExistente.setEstatura(1.75);
        fichaExistente.setPeso(70.0);
        fichaExistente.setGrupoSanguineo("O+");
    }

    // ─── OBTENER POR USUARIO ─────────────────────────────────────────────────

    @Test
    @DisplayName("Obtener por usuario existente retorna la ficha")
    void obtenerPorUsuario_usuarioConFicha_retornaFicha() {
        when(repo.findByUsuarioId("USR001")).thenReturn(Optional.of(fichaExistente));

        FichaMedica resultado = service.obtenerPorUsuario("USR001");

        assertNotNull(resultado);
        assertEquals("USR001", resultado.getUsuarioId());
        assertEquals("O+", resultado.getGrupoSanguineo());
    }

    @Test
    @DisplayName("Obtener por usuario sin ficha retorna null")
    void obtenerPorUsuario_usuarioSinFicha_retornaNull() {
        when(repo.findByUsuarioId("USR999")).thenReturn(Optional.empty());

        FichaMedica resultado = service.obtenerPorUsuario("USR999");

        assertNull(resultado);
    }

    // ─── GUARDAR (CREAR) ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Guardar para usuario sin ficha previa crea una nueva")
    void guardar_usuarioSinFichaPrevia_creaNuevaFicha() {
        FichaMedica datosNuevos = new FichaMedica();
        datosNuevos.setEstatura(1.80);
        datosNuevos.setPeso(80.0);
        datosNuevos.setGrupoSanguineo("A+");
        datosNuevos.setAlergias("Penicilina");

        when(repo.findByUsuarioId("USR002")).thenReturn(Optional.empty());
        when(repo.save(any(FichaMedica.class))).thenAnswer(inv -> inv.getArgument(0));

        FichaMedica resultado = service.guardar("USR002", datosNuevos);

        assertEquals("USR002", resultado.getUsuarioId());
        assertEquals(1.80, resultado.getEstatura());
        assertEquals("A+", resultado.getGrupoSanguineo());
        assertEquals("Penicilina", resultado.getAlergias());
        verify(repo, times(1)).save(any(FichaMedica.class));
    }

    // ─── GUARDAR (ACTUALIZAR) ────────────────────────────────────────────────

    @Test
    @DisplayName("Guardar para usuario con ficha previa actualiza la existente, no crea otra")
    void guardar_usuarioConFichaPrevia_actualizaExistente() {
        FichaMedica datosActualizados = new FichaMedica();
        datosActualizados.setEstatura(1.76);
        datosActualizados.setPeso(72.0);
        datosActualizados.setGrupoSanguineo("O+");
        datosActualizados.setPresionArterial("120/80");

        when(repo.findByUsuarioId("USR001")).thenReturn(Optional.of(fichaExistente));
        when(repo.save(any(FichaMedica.class))).thenAnswer(inv -> inv.getArgument(0));

        FichaMedica resultado = service.guardar("USR001", datosActualizados);

        // Debe seguir siendo el mismo registro (mismo ID), con los valores nuevos
        assertEquals(1L, resultado.getId());
        assertEquals(72.0, resultado.getPeso());
        assertEquals("120/80", resultado.getPresionArterial());
        verify(repo, times(1)).save(fichaExistente);
    }

    @Test
    @DisplayName("Guardar conserva todos los campos clinicos enviados")
    void guardar_conservaTodosLosCamposClinicos() {
        FichaMedica datos = new FichaMedica();
        datos.setCondicionesCronicas("Hipertension");
        datos.setMedicamentosActuales("Losartan");
        datos.setCirugiasPrevias("Apendicectomia 2018");
        datos.setAntecedentesFamiliares("Diabetes paterna");
        datos.setHabitoTabaco("No");
        datos.setHabitoAlcohol("Ocasional");
        datos.setEmergenciaNombre("Ana Perez");
        datos.setEmergenciaTelefono("+56912345678");
        datos.setEmergenciaRelacion("Esposa");

        when(repo.findByUsuarioId("USR003")).thenReturn(Optional.empty());
        when(repo.save(any(FichaMedica.class))).thenAnswer(inv -> inv.getArgument(0));

        FichaMedica resultado = service.guardar("USR003", datos);

        assertEquals("Hipertension", resultado.getCondicionesCronicas());
        assertEquals("Losartan", resultado.getMedicamentosActuales());
        assertEquals("Esposa", resultado.getEmergenciaRelacion());
        assertEquals("+56912345678", resultado.getEmergenciaTelefono());
    }
}
