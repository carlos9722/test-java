package com.empresa.polizas.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class IpcPropertiesTest {

    @Autowired
    private IpcProperties ipcProperties;

    @Test
    void elIpcSeLeeDeLaConfiguracionExterna() {
        assertEquals(0, new BigDecimal("0.05").compareTo(ipcProperties.ipc()));
    }
}