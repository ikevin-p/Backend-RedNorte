package com.rednorte.msauditoria;

import com.rednorte.msauditoria.model.RegistroAuditoria;
import com.rednorte.msauditoria.repository.AuditoriaRepository;
import com.rednorte.msauditoria.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository repository;

    @InjectMocks
    private AuditoriaService service;

    private RegistroAuditoria registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroAuditoria();
        registro.setId(1L);
        registro.setAccion(RegistroAuditoria.TipoAccion.LOGIN);
        registro.setModulo(RegistroAuditoria.ModuloSistema.USUARIOS);
        registro.setUsuarioId("USR001");
        registro.setResultado(RegistroAuditoria.ResultadoAccion.EXITOSO);
    }

    @Test
    void registrar_guardaElRegistro() {
        when(repository.save(any(RegistroAuditoria.class))).thenReturn(registro);
        RegistroAuditoria res = service.registrar(registro);
        assertNotNull(res);
        assertEquals("USR001", res.getUsuarioId());
        verify(repository, times(1)).save(registro);
    }

    @Test
    void buscarPorId_existente_retornaRegistro() {
        when(repository.findById(1L)).thenReturn(Optional.of(registro));
        Optional<RegistroAuditoria> res = service.buscarPorId(1L);
        assertTrue(res.isPresent());
        assertEquals(RegistroAuditoria.TipoAccion.LOGIN, res.get().getAccion());
    }

    @Test
    void porUsuario_retornaHistorial() {
        when(repository.findByUsuarioIdOrderByFechaHoraDesc("USR001")).thenReturn(List.of(registro));
        List<RegistroAuditoria> res = service.porUsuario("USR001");
        assertEquals(1, res.size());
    }

    @Test
    void porModulo_convierteStringAEnum() {
        when(repository.findByModuloOrderByFechaHoraDesc(RegistroAuditoria.ModuloSistema.USUARIOS))
            .thenReturn(List.of(registro));
        List<RegistroAuditoria> res = service.porModulo("usuarios");
        assertEquals(1, res.size());
    }

    @Test
    void porAccion_invalida_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.porAccion("ACCION_INEXISTENTE"));
    }

    @Test
    void listarRecientes_retornaLista() {
        when(repository.findTop100ByOrderByFechaHoraDesc()).thenReturn(List.of(registro));
        List<RegistroAuditoria> res = service.listarRecientes();
        assertFalse(res.isEmpty());
    }
}
