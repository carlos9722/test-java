package com.empresa.polizas.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        List<String> detalles) {
}