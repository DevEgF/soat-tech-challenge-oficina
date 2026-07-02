package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.api.dto.LoginRequest
import com.soat.tech.challenge.oficina.application.api.dto.LoginResponse
import com.soat.tech.challenge.oficina.domain.port.TokenIssuerPort
import jakarta.validation.Valid
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/auth")
class AuthController(
	private val authenticationManager: AuthenticationManager,
	private val jwtIssuer: TokenIssuerPort,
) {

	@PostMapping("/login")
	fun login(@Valid @RequestBody req: LoginRequest): LoginResponse {
		authenticationManager.authenticate(
			UsernamePasswordAuthenticationToken(req.username, req.password),
		)
		val issued = jwtIssuer.issueForUser(req.username)
		return LoginResponse(accessToken = issued.token, expiresInSeconds = issued.expiresInSeconds)
	}
}
