package com.empresa.polizas.dto;

import com.empresa.polizas.entity.Poliza;
import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.TipoPoliza;

import java.math.BigDecimal;

public record PolizaResponse(
        Long id,
        String numeroPoliza,
        TipoPoliza tipo,
        EstadoPoliza estado,
        BigDecimal canon,
        BigDecimal prima) {

    public static PolizaResponse from(Poliza poliza) {
        return new PolizaResponse(
                poliza.getId(),
                poliza.getNumeroPoliza(),
                poliza.getTipo(),
                poliza.getEstado(),
                poliza.getCanon(),
                poliza.getPrima());
    }
}