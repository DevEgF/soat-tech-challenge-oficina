package com.soat.tech.challenge.oficina.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class JacksonKotlinConfiguration {

	@Bean
	@Primary
	fun objectMapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
}
