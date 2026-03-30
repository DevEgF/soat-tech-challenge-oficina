package com.soat.tech.challenge.oficina.infrastructure

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {

	@Bean
	fun openAPI(): OpenAPI = OpenAPI()
		.info(
			Info()
				.title("Oficina — Tech Challenge SOAT")
				.description("API REST do MVP: ordens de serviço, clientes, peças e métricas. Rotas `/api/admin/**` exigem JWT (login em `/api/public/auth/login`).")
				.version("1.0.0"),
		)
		.components(
			Components().addSecuritySchemes(
				"bearer-jwt",
				SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT"),
			),
		)
}
