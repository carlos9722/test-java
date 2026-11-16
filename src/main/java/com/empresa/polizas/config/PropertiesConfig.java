package com.empresa.polizas.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({IpcProperties.class, ApiKeyProperties.class})
public class PropertiesConfig {
}