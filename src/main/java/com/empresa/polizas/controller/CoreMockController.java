package com.empresa.polizas.controller;

import com.empresa.polizas.dto.CoreEventoRequest;
import com.empresa.polizas.service.CoreEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private final CoreEventService coreEventService;

    public CoreMockController(CoreEventService coreEventService) {
        this.coreEventService = coreEventService;
    }

    @PostMapping("/evento")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        coreEventService.enviarEvento(request.evento(), request.polizaId());
    }
}