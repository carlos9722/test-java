package com.empresa.polizas.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class DatosSemillaTest {

    @Autowired
    private EntityManager em;

    @Test
    void seCarganSeisPolizasYNueveRiesgos() {
        Long polizas = em.createQuery("select count(p) from Poliza p", Long.class).getSingleResult();
        Long riesgos = em.createQuery("select count(r) from Riesgo r", Long.class).getSingleResult();

        assertEquals(6L, polizas);
        assertEquals(9L, riesgos);
    }

    @Test
    void laPolizaColectivaActivaTieneTresRiesgos() {
        Poliza poliza = em.createQuery(
                "select p from Poliza p where p.numeroPoliza = 'POL-002'", Poliza.class).getSingleResult();

        assertEquals(3, poliza.getRiesgos().size());
    }
}