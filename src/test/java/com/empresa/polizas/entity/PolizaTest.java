package com.empresa.polizas.entity;

import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.EstadoRiesgo;
import com.empresa.polizas.enums.TipoPoliza;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolizaTest {

    private Poliza crearPoliza() {
        return new Poliza("POL-TEST", TipoPoliza.COLECTIVA,
                new BigDecimal("1000.00"), new BigDecimal("100.00"));
    }

    @Test
    void agregarRiesgoSincronizaAmbosLadosDeLaRelacion() {
        Poliza poliza = crearPoliza();
        Riesgo riesgo = new Riesgo("Incendio");

        poliza.agregarRiesgo(riesgo);

        assertEquals(1, poliza.getRiesgos().size());
        assertSame(poliza, riesgo.getPoliza());
    }

    @Test
    void cancelarCambiaEstadoYCancelaTodosLosRiesgos() {
        Poliza poliza = crearPoliza();
        poliza.agregarRiesgo(new Riesgo("Incendio"));
        poliza.agregarRiesgo(new Riesgo("Robo"));

        poliza.cancelar();

        assertEquals(EstadoPoliza.CANCELADA, poliza.getEstado());
        assertTrue(poliza.getRiesgos().stream()
                .allMatch(r -> r.getEstado() == EstadoRiesgo.CANCELADA));
    }

    @Test
    void renovarIncrementaCanonYPrimaConElIpcYCambiaEstado() {
        Poliza poliza = crearPoliza();

        poliza.renovar(new BigDecimal("0.05"));

        assertEquals(new BigDecimal("1050.00"), poliza.getCanon());
        assertEquals(new BigDecimal("105.00"), poliza.getPrima());
        assertEquals(EstadoPoliza.RENOVADA, poliza.getEstado());
    }

    @Test
    void renovarRedondeaHalfUpADosDecimales() {
        Poliza poliza = new Poliza("POL-R", TipoPoliza.INDIVIDUAL,
                new BigDecimal("100.10"), new BigDecimal("10.00"));

        poliza.renovar(new BigDecimal("0.0575"));

        assertEquals(new BigDecimal("105.86"), poliza.getCanon());
    }
}