package com.empresa.polizas.repository;

import com.empresa.polizas.entity.Riesgo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class RiesgoRepositoryTest {

    @Autowired
    private RiesgoRepository riesgoRepository;

    @Test
    void findByIdDevuelveElRiesgoSemilla() {
        Riesgo riesgo = riesgoRepository.findAll().get(0);

        assertTrue(riesgoRepository.findById(riesgo.getId()).isPresent());
    }

    @Test
    void findByIdInexistenteDevuelveOptionalVacio() {
        assertTrue(riesgoRepository.findById(9999L).isEmpty());
    }
}