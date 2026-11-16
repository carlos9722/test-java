package com.empresa.polizas.integration;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.empresa.polizas.service.CoreEventService;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:testdb-${random.uuid}")
@AutoConfigureMockMvc
@Transactional
class PolizasApiIntegrationTest {

    private static final String API_KEY = "123456";
    private static final String JSON_RIESGO = "{\"descripcion\":\"Terremoto\"}";

    @Autowired
    private MockMvc mockMvc;

    private ResultActions obtener(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.get(url).header("x-api-key", API_KEY));
    }

    private ResultActions enviar(String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url).header("x-api-key", API_KEY));
    }

    private ResultActions enviarJson(String url, String json) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
                .header("x-api-key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }


    @Test
    void sinApiKeyResponde401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/polizas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void conApiKeyIncorrectaResponde401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/polizas").header("x-api-key", "mala"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unaRutaInexistenteSinApiKeyTambienResponde401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/no-existe"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void listarSinFiltrosDevuelveLasSeisPolizas() throws Exception {
        obtener("/polizas")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void listarFiltraSoloPorTipo() throws Exception {
        obtener("/polizas?tipo=INDIVIDUAL")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].tipo", everyItem(is("INDIVIDUAL"))));
    }

    @Test
    void listarFiltraSoloPorEstado() throws Exception {
        obtener("/polizas?estado=CANCELADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].estado", everyItem(is("CANCELADA"))));
    }

    @Test
    void listarCombinaTipoYEstado() throws Exception {
        obtener("/polizas?tipo=COLECTIVA&estado=ACTIVA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].numeroPoliza").value("POL-002"));
    }

    @Test
    void listarConTipoInvalidoResponde400() throws Exception {
        obtener("/polizas?tipo=XYZ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void riesgosDeUnaPolizaExistente() throws Exception {
        obtener("/polizas/2/riesgos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].polizaId", everyItem(is(2))));
    }

    @Test
    void riesgosDeUnaPolizaInexistenteResponde404() throws Exception {
        obtener("/polizas/999/riesgos")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }


    @Test
    void renovarAplicaElIpcAlCanonYALaPrima() throws Exception {
        enviar("/polizas/1/renovar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"))
                .andExpect(jsonPath("$.canon").value(1050000.0))
                .andExpect(jsonPath("$.prima").value(52500.0));
    }

    @Test
    void renovarUnaPolizaCanceladaResponde400() throws Exception {
        enviar("/polizas/3/renovar")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void renovarUnaPolizaInexistenteResponde404() throws Exception {
        enviar("/polizas/999/renovar").andExpect(status().isNotFound());
    }


    @Test
    void cancelarUnaPolizaCancelaTodosSusRiesgos() throws Exception {
        enviar("/polizas/2/cancelar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        obtener("/polizas/2/riesgos")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].estado", everyItem(is("CANCELADA"))));
    }

    @Test
    void cancelarUnaPolizaYaCanceladaResponde400() throws Exception {
        enviar("/polizas/4/cancelar").andExpect(status().isBadRequest());
    }

    @Test
    void cancelarUnaPolizaInexistenteResponde404() throws Exception {
        enviar("/polizas/999/cancelar").andExpect(status().isNotFound());
    }


    @Test
    void agregarRiesgoAUnaPolizaColectivaResponde201YQuedaPersistido() throws Exception {
        enviarJson("/polizas/2/riesgos", JSON_RIESGO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.polizaId").value(2));

        obtener("/polizas/2/riesgos").andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void agregarRiesgoAUnaPolizaIndividualConRiesgoResponde400() throws Exception {
        enviarJson("/polizas/1/riesgos", JSON_RIESGO).andExpect(status().isBadRequest());

        obtener("/polizas/1/riesgos").andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void agregarRiesgoAUnaPolizaIndividualSinRiesgosTambienResponde400() throws Exception {
        enviarJson("/polizas/6/riesgos", JSON_RIESGO).andExpect(status().isBadRequest());

        obtener("/polizas/6/riesgos").andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void agregarRiesgoAUnaPolizaCanceladaResponde400() throws Exception {
        enviarJson("/polizas/4/riesgos", JSON_RIESGO).andExpect(status().isBadRequest());
    }

    @Test
    void agregarRiesgoConDescripcionVaciaResponde400ConDetalle() throws Exception {
        enviarJson("/polizas/2/riesgos", "{\"descripcion\":\"\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0]", startsWith("descripcion")));
    }

    @Test
    void agregarRiesgoAUnaPolizaInexistenteResponde404() throws Exception {
        enviarJson("/polizas/999/riesgos", JSON_RIESGO).andExpect(status().isNotFound());
    }

    // ---------- POST /riesgos/{id}/cancelar ----------

    @Test
    void cancelarUnRiesgoNoCancelaLaPoliza() throws Exception {
        enviar("/riesgos/2/cancelar")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        // La póliza 2 sigue ACTIVA (las 3 activas semilla: 1, 2 y 6)
        obtener("/polizas?estado=ACTIVA").andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void cancelarUnRiesgoYaCanceladoResponde400() throws Exception {
        enviar("/riesgos/2/cancelar").andExpect(status().isOk());
        enviar("/riesgos/2/cancelar").andExpect(status().isBadRequest());
    }

    @Test
    void cancelarUnRiesgoInexistenteResponde404() throws Exception {
        enviar("/riesgos/999/cancelar").andExpect(status().isNotFound());
    }

    // ---------- POST /core-mock/evento ----------

    @Test
    void coreMockResponde204YRegistraElEventoEnElLog() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(CoreEventService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            enviarJson("/core-mock/evento", "{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}")
                    .andExpect(status().isNoContent());

            assertEquals(1, appender.list.size());
            String mensaje = appender.list.get(0).getFormattedMessage();
            assertTrue(mensaje.contains("ACTUALIZACION") && mensaje.contains("555"));
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    void coreMockConEventoDesconocidoResponde400() throws Exception {
        enviarJson("/core-mock/evento", "{\"evento\":\"OTRO\",\"polizaId\":555}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void coreMockSinPolizaIdResponde400() throws Exception {
        enviarJson("/core-mock/evento", "{\"evento\":\"ACTUALIZACION\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles", hasSize(1)));
    }
}