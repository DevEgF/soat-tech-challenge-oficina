package com.soat.tech.challenge.oficina.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

	@Bean
	fun securityFilterChain(
		http: HttpSecurity,
		@Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") issuerUri: String,
		@Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") jwkSetUri: String,
	): SecurityFilterChain {
		val oauth2Configured = issuerUri.isNotBlank() || jwkSetUri.isNotBlank()
		http.authorizeHttpRequests { authorize ->
			authorize.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
			if (oauth2Configured) {
				authorize.anyRequest().authenticated()
			} else {
				authorize.anyRequest().permitAll()
			}
		}
		if (oauth2Configured) {
			http.oauth2ResourceServer { oauth2 -> oauth2.jwt { } }
		}
		return http.build()
	}
}
