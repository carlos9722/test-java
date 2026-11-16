package com.empresa.polizas.repository;

import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.TipoPoliza;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class PolizaRepositoryTest {

    @Autowired
    private PolizaRepository polizaRepository;

    @Test
    void sinFiltrosDevuelveTodasLasPolizas() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(null, null);

        assertEquals(6, resultado.size());
    }

    @Test
    void filtraSoloPorTipo() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(TipoPoliza.INDIVIDUAL, null);

        assertEquals(3, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getTipo() == TipoPoliza.INDIVIDUAL));
    }

    @Test
    void filtraSoloPorEstado() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(null, EstadoPoliza.ACTIVA);

        assertEquals(3, resultado.size());
        assertTrue(resultado.stream().allMatch(p -> p.getEstado() == EstadoPoliza.ACTIVA));
    }

    @Test
    void combinaTipoYEstado() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(TipoPoliza.COLECTIVA, EstadoPoliza.ACTIVA);

        assertEquals(1, resultado.size());
        assertEquals("POL-002", resultado.get(0).getNumeroPoliza());
    }

    @Test
    void combinacionSinCoincidenciasDevuelveListaVacia() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(TipoPoliza.INDIVIDUAL, EstadoPoliza.RENOVADA);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void resultadosVienenOrdenadosPorId() {
        List<Poliza> resultado = polizaRepository.buscarPorFiltros(null, null);

        for (int i = 1; i < resultado.size(); i++) {
            assertTrue(resultado.get(i - 1).getId() < resultado.get(i).getId());
        }
    }
}