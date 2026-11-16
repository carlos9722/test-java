package com.empresa.polizas.controller;

import com.empresa.polizas.dto.RiesgoResponse;
import com.empresa.polizas.service.RiesgoService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/riesgos")
public class RiesgoController {

    private final RiesgoService riesgoService;

    public RiesgoController(RiesgoService riesgoService) {
        this.riesgoService = riesgoService;
    }

    @PostMapping("/{id}/cancelar")
    public RiesgoResponse cancelar(@PathVariable("id") Long id) {
        return riesgoService.cancelar(id);
    }
}