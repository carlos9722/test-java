
package com.empresa.polizas.service;

import com.empresa.polizas.config.IpcProperties;
import com.empresa.polizas.dto.PolizaResponse;
import com.empresa.polizas.dto.RiesgoResponse;
import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.entity.Riesgo;
import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.EstadoRiesgo;
import com.empresa.polizas.enums.TipoPoliza;
import com.empresa.polizas.enums.EventoCore;
import com.empresa.polizas.exception.RecursoNoEncontradoException;
import com.empresa.polizas.exception.ReglaNegocioException;
import com.empresa.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private CoreEventService coreEventService;

    private PolizaService service;

    @BeforeEach
    void setUp() {
        service = new PolizaService(polizaRepository,
                new IpcProperties(new BigDecimal("0.05")), coreEventService);
    }

    private Poliza polizaColectiva() {
        return new Poliza("POL-T", TipoPoliza.COLECTIVA,
                new BigDecimal("1000.00"), new BigDecimal("100.00"));
    }

    @Test
    void listarDelegaLosFiltrosYMapeaADto() {
        when(polizaRepository.buscarPorFiltros(
                TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA))
                .thenReturn(List.of(polizaColectiva()));

        List<PolizaResponse> resultado =
                service.listar(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);

        assertEquals(1, resultado.size());
        assertEquals("POL-T", resultado.get(0).numeroPoliza());

        verify(polizaRepository)
                .buscarPorFiltros(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);
    }

    @Test
    void obtenerRiesgosDevuelveLosRiesgosDeLaPoliza() {
        Poliza poliza = polizaColectiva();
        poliza.agregarRiesgo(new Riesgo("Incendio"));
        poliza.agregarRiesgo(new Riesgo("Robo"));

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        List<RiesgoResponse> riesgos = service.obtenerRiesgos(1L);

        assertEquals(2, riesgos.size());
        assertEquals("Incendio", riesgos.get(0).descripcion());
        assertEquals("Robo", riesgos.get(1).descripcion());
    }

    @Test
    void obtenerRiesgosDePolizaInexistenteLanzaNoEncontrado() {
        when(polizaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtenerRiesgos(99L));
    }

    @Test
    void renovarAplicaElIpcAlCanonYALaPrimaYCambiaElEstado() {
        Poliza poliza = polizaColectiva();

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        PolizaResponse respuesta = service.renovar(1L);

        assertEquals(new BigDecimal("1050.00"), respuesta.canon());
        assertEquals(new BigDecimal("105.00"), respuesta.prima());
        assertEquals(EstadoPoliza.RENOVADA, respuesta.estado());
    }

    @Test
    void renovarUnaPolizaYaRenovadaEstaPermitidoYCompone() {
        Poliza poliza = polizaColectiva();

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        service.renovar(1L);
        PolizaResponse segunda = service.renovar(1L);

        assertEquals(new BigDecimal("1102.50"), segunda.canon());
        assertEquals(new BigDecimal("110.25"), segunda.prima());
    }

    @Test
    void renovarPolizaCanceladaLanzaReglaDeNegocioYNoModificaValores() {
        Poliza poliza = polizaColectiva();
        poliza.cancelar();

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.renovar(1L));

        assertEquals(new BigDecimal("1000.00"), poliza.getCanon());
        assertEquals(new BigDecimal("100.00"), poliza.getPrima());
        assertEquals(EstadoPoliza.CANCELADA, poliza.getEstado());
    }

    @Test
    void renovarPolizaInexistenteLanzaNoEncontrado() {
        when(polizaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.renovar(99L));
    }

    @Test
    void renovarNotificaAlCoreConElIdDeLaPoliza() {
        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(polizaColectiva()));

        service.renovar(1L);

        verify(coreEventService)
                .enviarEvento(EventoCore.ACTUALIZACION, 1L);
    }

    @Test
    void renovarPolizaCanceladaNoNotificaAlCore() {
        Poliza poliza = polizaColectiva();
        poliza.cancelar();

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.renovar(1L));

        verify(coreEventService, never())
                .enviarEvento(any(), any());
    }

    @Test
    void cancelarCambiaElEstadoYCancelaTodosLosRiesgos() {
        Poliza poliza = polizaColectiva();
        poliza.agregarRiesgo(new Riesgo("Incendio"));
        poliza.agregarRiesgo(new Riesgo("Robo"));

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        PolizaResponse respuesta = service.cancelar(1L);

        assertEquals(EstadoPoliza.CANCELADA, respuesta.estado());

        assertTrue(poliza.getRiesgos().stream()
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADA));
    }

    @Test
    void cancelarPolizaYaCanceladaLanzaReglaDeNegocio() {
        Poliza poliza = polizaColectiva();
        poliza.cancelar();

        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.cancelar(1L));
    }

    @Test
    void cancelarPolizaInexistenteLanzaNoEncontrado() {
        when(polizaRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.cancelar(99L));
    }

    @Test
    void cancelarNotificaAlCore() {
        when(polizaRepository.findById(1L))
                .thenReturn(Optional.of(polizaColectiva()));

        service.cancelar(1L);

        verify(coreEventService)
                .enviarEvento(EventoCore.ACTUALIZACION, 1L);
    }
}
