package com.dormfix.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * During development the Compose frontend runs on localhost:3000 and the backend on
 * localhost:8080. Browsers block requests between different ports unless the backend
 * allows it (CORS), so this lets the frontend call /api/ endpoints.
 *
 * This file is Kotlin while the rest of the server is Java, which shows both
 * languages compile together in the same module.
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
    }
}
