package com.rednorte.notificaciones.service;

import com.rednorte.notificaciones.factory.CambioEstadoConsultaCreator;
import com.rednorte.notificaciones.factory.NotificacionCreator;
import com.rednorte.notificaciones.factory.ReasignacionCreator;
import com.rednorte.notificaciones.model.Notificacion;
import com.rednorte.notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitarios — NotificacionService")
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository repo;

    // NotificacionService depende de una List<NotificacionCreator> que
    // Spring inyecta automaticamente en produccion (uno por cada @Component
    // que implemente la interfaz). En el test se construye manualmente con
    // las implementaciones reales del paquete factory/, ya que son simples
    // y deterministas: esto verifica la integracion real Service + Factory,
    // no solo el Service de forma aislada.
    private NotificacionService service;

    private Notificacion notificacionMock;

    @BeforeEach
    void setUp() {
        notificacionMock = new Notificacion("USR001", "Titulo", "Mensaje", "INFO", 1L);
        notificacionMock.setId(1L);

        service = new NotificacionService();
        ReflectionTestUtils.setField(service, "repo", repo);
        ReflectionTestUtils.setField(service, "creators", List.of(
                new CambioEstadoConsultaCreator(),
                new ReasignacionCreator()
        ));
    }

    // ─── CREAR ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Crear notificacion delega en el repositorio")
    void crear_delegaEnRepositorio() {
        when(repo.save(any(Notificacion.class))).thenReturn(notificacionMock);

        Notificacion resultado = service.crear(notificacionMock);

        assertEquals("USR001", resultado.getUsuarioId());
        verify(repo, times(1)).save(notificacionMock);
    }

    // ─── LISTAR ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Listar por usuario retorna notificaciones ordenadas")
    void listarPorUsuario_retornaNotificaciones() {
        when(repo.findByUsuarioIdOrderByFechaCreacionDesc("USR001"))
                .thenReturn(List.of(notificacionMock));

        List<Notificacion> resultado = service.listarPorUsuario("USR001");

        assertEquals(1, resultado.size());
        assertEquals("USR001", resultado.get(0).getUsuarioId());
    }

    @Test
    @DisplayName("Listar no leidas retorna solo las no leidas")
    void listarNoLeidas_retornaSoloNoLeidas() {
        when(repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc("USR001"))
                .thenReturn(List.of(notificacionMock));

        List<Notificacion> resultado = service.listarNoLeidas("USR001");

        assertEquals(1, resultado.size());
        assertFalse(resultado.get(0).isLeida());
    }

    @Test
    @DisplayName("Contar no leidas retorna el numero correcto")
    void contarNoLeidas_retornaNumeroCorrecto() {
        when(repo.countByUsuarioIdAndLeidaFalse("USR001")).thenReturn(5L);

        long resultado = service.contarNoLeidas("USR001");

        assertEquals(5L, resultado);
    }

    // ─── MARCAR LEIDA ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Marcar leida cambia el estado a true y guarda")
    void marcarLeida_notificacionExistente_marcaComoLeida() {
        when(repo.findById(1L)).thenReturn(Optional.of(notificacionMock));
        when(repo.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.marcarLeida(1L);

        assertTrue(resultado.isLeida());
        verify(repo, times(1)).save(notificacionMock);
    }

    @Test
    @DisplayName("Marcar leida con ID inexistente lanza excepcion")
    void marcarLeida_idInexistente_lanzaExcepcion() {
        when(repo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.marcarLeida(999L));
    }

    // ─── MARCAR TODAS LEIDAS ─────────────────────────────────────────────────

    @Test
    @DisplayName("Marcar todas leidas actualiza cada notificacion no leida")
    void marcarTodasLeidas_actualizaTodasLasNoLeidas() {
        Notificacion n2 = new Notificacion("USR001", "T2", "M2", "INFO", 2L);
        when(repo.findByUsuarioIdAndLeidaFalseOrderByFechaCreacionDesc("USR001"))
                .thenReturn(List.of(notificacionMock, n2));

        service.marcarTodasLeidas("USR001");

        assertTrue(notificacionMock.isLeida());
        assertTrue(n2.isLeida());
        verify(repo, times(1)).saveAll(List.of(notificacionMock, n2));
    }

    // ─── NOTIFICAR CAMBIO DE ESTADO ──────────────────────────────────────────

    @ParameterizedTest
    @DisplayName("Notificar cambio de estado asigna el tipo correcto segun el nuevo estado")
    @CsvSource({
        "AGENDADA, SUCCESS",
        "ATENDIDA, SUCCESS",
        "CANCELADA, ERROR",
        "REASIGNADA, WARNING",
        "PENDIENTE, INFO"
    })
    void notificarCambioEstado_asignaTipoSegunEstado(String estadoNuevo, String tipoEsperado) {
        when(repo.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.notificarCambioEstado("USR001", 10L, "PENDIENTE", estadoNuevo);

        assertEquals(tipoEsperado, resultado.getTipo());
        assertEquals("USR001", resultado.getUsuarioId());
        assertEquals(10L, resultado.getConsultaId());
        assertTrue(resultado.getMensaje().contains(estadoNuevo));
    }

    // ─── NOTIFICAR REASIGNACION (Factory Method) ────────────────────────────

    @Test
    @DisplayName("Notificar reasignacion construye notificacion de tipo SUCCESS via ReasignacionCreator")
    void notificarReasignacion_construyeNotificacionExitosa() {
        when(repo.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion resultado = service.notificarReasignacion("USR002", 20L, "cardiologia");

        assertEquals("SUCCESS", resultado.getTipo());
        assertEquals("USR002", resultado.getUsuarioId());
        assertEquals(20L, resultado.getConsultaId());
        assertTrue(resultado.getMensaje().contains("cardiologia"));
    }

    @Test
    @DisplayName("Factory Method: si no hay creator registrado para el tipo de evento, lanza excepcion clara")
    void factoryMethod_tipoEventoNoRegistrado_lanzaExcepcion() {
        // Servicio con un solo creator registrado, simulando que falta
        // el de reasignacion (caso de configuracion incompleta).
        NotificacionService servicioIncompleto = new NotificacionService();
        ReflectionTestUtils.setField(servicioIncompleto, "repo", repo);
        ReflectionTestUtils.setField(servicioIncompleto, "creators",
                List.<NotificacionCreator>of(new CambioEstadoConsultaCreator()));

        assertThrows(IllegalArgumentException.class,
                () -> servicioIncompleto.notificarReasignacion("USR003", 30L, "pediatria"));
    }
}
