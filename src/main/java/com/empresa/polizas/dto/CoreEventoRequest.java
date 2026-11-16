package com.empresa.polizas.dto;

import com.empresa.polizas.enums.EventoCore;
import jakarta.validation.constraints.NotNull;

public record CoreEventoRequest(
        @NotNull(message = "El evento es obligatorio") EventoCore evento,
        @NotNull(message = "El polizaId es obligatorio") Long polizaId) {
}