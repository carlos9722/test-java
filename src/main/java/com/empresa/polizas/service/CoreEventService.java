package com.empresa.polizas.service;

import com.empresa.polizas.enums.EventoCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CoreEventService {

    private static final Logger log = LoggerFactory.getLogger(CoreEventService.class);

    public void enviarEvento(EventoCore evento, Long polizaId) {
        log.info("[CORE-MOCK] Intento de envío al CORE: evento={}, polizaId={}", evento, polizaId);
    }
}