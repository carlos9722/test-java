package com.empresa.polizas.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.empresa.polizas.enums.EventoCore;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreEventServiceTest {

    @Test
    void registraEnElLogElEventoYElIdDeLaPoliza() {
        Logger logger = (Logger) LoggerFactory.getLogger(CoreEventService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            new CoreEventService().enviarEvento(EventoCore.ACTUALIZACION, 555L);

            assertEquals(1, appender.list.size());
            String mensaje = appender.list.get(0).getFormattedMessage();
            assertTrue(mensaje.contains("ACTUALIZACION"));
            assertTrue(mensaje.contains("555"));
        } finally {
            logger.detachAppender(appender);
        }
    }
}