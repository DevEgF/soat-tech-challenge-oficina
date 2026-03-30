package com.soat.tech.challenge.oficina.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun userDetailsService(
		@Value("\${app.security.admin.username}") username: String,
		@Value("\${app.security.admin.password}") passwordRaw: String,
		encoder: PasswordEncoder,
	): UserDetailsService {
		val user = User.builder()
			.username(username)
			.password(encoder.encode(passwordRaw))
			.roles("ADMIN")
			.build()
		return InMemoryUserDetailsManager(user)
	}

	@Bean
	fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager =
		config.authenticationManager

	@Bean
	fun securityFilterChain(http: HttpSecurity, jwtDecoder: JwtDecoder): SecurityFilterChain {
		http.csrf { it.disable() }
		http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
		http.authorizeHttpRequests { authorize ->
			authorize.requestMatchers(
				"/actuator/health",
				"/actuator/health/**",
				"/v3/api-docs",
				"/v3/api-docs/**",
				"/swagger-ui/**",
				"/swagger-ui.html",
				"/api/public/auth/login",
			).permitAll()
			authorize.requestMatchers(HttpMethod.GET, "/api/public/os/**").permitAll()
			authorize.requestMatchers("/api/admin/**").hasAuthority("SCOPE_ADMIN")
			authorize.anyRequest().denyAll()
		}
		http.oauth2ResourceServer { oauth2 -> oauth2.jwt { it.decoder(jwtDecoder) } }
		return http.build()
	}
}
