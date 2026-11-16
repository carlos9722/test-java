package com.empresa.polizas.dto;

import com.empresa.polizas.entity.Riesgo;
import com.empresa.polizas.enums.EstadoRiesgo;

public record RiesgoResponse(
        Long id,
        String descripcion,
        EstadoRiesgo estado,
        Long polizaId) {

    public static RiesgoResponse from(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getDescripcion(),
                riesgo.getEstado(),
                riesgo.getPoliza().getId());
    }
}