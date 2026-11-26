package com.coworkhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ConfiguracionAplicacion {

    /**
     * Reloj de negocio. Las reglas de fechas usan esta zona horaria en vez de
     * la del servidor, para que las pruebas y el calculo de anticipacion sean estables.
     */
    @Bean
    public Clock reloj(@Value("${coworkhub.reloj.zona:America/Argentina/Buenos_Aires}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
