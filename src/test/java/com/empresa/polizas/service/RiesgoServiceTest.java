package com.empresa.polizas.service;

import com.empresa.polizas.dto.CrearRiesgoRequest;
import com.empresa.polizas.dto.RiesgoResponse;
import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.entity.Riesgo;
import com.empresa.polizas.enums.EstadoRiesgo;
import com.empresa.polizas.enums.EventoCore;
import com.empresa.polizas.enums.TipoPoliza;
import com.empresa.polizas.exception.RecursoNoEncontradoException;
import com.empresa.polizas.exception.ReglaNegocioException;
import com.empresa.polizas.repository.PolizaRepository;
import com.empresa.polizas.repository.RiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiesgoServiceTest {

    @Mock
    private PolizaRepository polizaRepository;
    @Mock
    private RiesgoRepository riesgoRepository;
    @Mock
    private CoreEventService coreEventService;

    private RiesgoService service;

    @BeforeEach
    void setUp() {
        service = new RiesgoService(polizaRepository, riesgoRepository, coreEventService);
    }

    private Poliza poliza(TipoPoliza tipo) {
        return new Poliza("POL-T", tipo, new BigDecimal("1000.00"), new BigDecimal("100.00"));
    }


    @Test
    void agregarRiesgoAPolizaColectivaLoAsociaLoGuardaYNotificaAlCore() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        RiesgoResponse respuesta = service.agregar(1L, new CrearRiesgoRequest("Incendio"));

        assertEquals("Incendio", respuesta.descripcion());
        assertEquals(EstadoRiesgo.ACTIVA, respuesta.estado());
        assertEquals(1, poliza.getRiesgos().size());
        verify(riesgoRepository).save(any(Riesgo.class));
        verify(coreEventService).enviarEvento(EventoCore.ACTUALIZACION, 1L);
    }

    @Test
    void agregarRiesgoAPolizaIndividualConUnRiesgoSeRechaza() {
        Poliza poliza = poliza(TipoPoliza.INDIVIDUAL);
        poliza.agregarRiesgo(new Riesgo("Existente"));
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.agregar(1L, new CrearRiesgoRequest("Otro")));

        assertEquals(1, poliza.getRiesgos().size());
        verify(riesgoRepository, never()).save(any());
        verify(coreEventService, never()).enviarEvento(any(), any());
    }

    @Test
    void agregarRiesgoAPolizaIndividualSinRiesgosTambienSeRechaza() {
        Poliza poliza = poliza(TipoPoliza.INDIVIDUAL);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.agregar(1L, new CrearRiesgoRequest("Otro")));

        assertEquals(0, poliza.getRiesgos().size());
    }

    @Test
    void agregarRiesgoAPolizaCanceladaSeRechaza() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        poliza.cancelar();
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        assertThrows(ReglaNegocioException.class,
                () -> service.agregar(1L, new CrearRiesgoRequest("Incendio")));

        assertEquals(0, poliza.getRiesgos().size());
        verify(coreEventService, never()).enviarEvento(any(), any());
    }

    @Test
    void agregarRiesgoAPolizaInexistenteLanzaNoEncontrado() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.agregar(99L, new CrearRiesgoRequest("Incendio")));
    }


    @Test
    void cancelarRiesgoCambiaElEstadoYNotificaAlCoreConLaPolizaAsociada() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        ReflectionTestUtils.setField(poliza, "id", 5L);
        Riesgo riesgo = new Riesgo("Robo");
        poliza.agregarRiesgo(riesgo);
        when(riesgoRepository.findById(10L)).thenReturn(Optional.of(riesgo));

        RiesgoResponse respuesta = service.cancelar(10L);

        assertEquals(EstadoRiesgo.CANCELADA, respuesta.estado());
        assertEquals(EstadoRiesgo.CANCELADA, riesgo.getEstado());
        verify(coreEventService).enviarEvento(EventoCore.ACTUALIZACION, 5L);
    }

    @Test
    void cancelarRiesgoYaCanceladoLanzaReglaDeNegocio() {
        Poliza poliza = poliza(TipoPoliza.COLECTIVA);
        Riesgo riesgo = new Riesgo("Robo");
        poliza.agregarRiesgo(riesgo);
        riesgo.cancelar();
        when(riesgoRepository.findById(10L)).thenReturn(Optional.of(riesgo));

        assertThrows(ReglaNegocioException.class, () -> service.cancelar(10L));
        verify(coreEventService, never()).enviarEvento(any(), any());
    }

    @Test
    void cancelarRiesgoInexistenteLanzaNoEncontrado() {
        when(riesgoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.cancelar(99L));
    }
}