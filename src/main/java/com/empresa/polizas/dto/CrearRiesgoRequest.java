package com.empresa.polizas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearRiesgoRequest(
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion) {
}