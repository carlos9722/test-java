package com.empresa.polizas.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "seguridad")
public record ApiKeyProperties(@NotBlank String apiKey) {
}