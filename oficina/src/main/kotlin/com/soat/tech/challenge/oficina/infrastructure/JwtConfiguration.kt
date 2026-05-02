package com.soat.tech.challenge.oficina.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import java.security.MessageDigest
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtConfiguration(
	@Value("\${app.jwt.secret}") private val secretRaw: String,
) {

	private val secretKey: SecretKey by lazy {
		val digest = MessageDigest.getInstance("SHA-256").digest(secretRaw.toByteArray(Charsets.UTF_8))
		SecretKeySpec(digest, "HmacSHA256")
	}

	@Bean
	fun jwtSigningKey(): SecretKey = secretKey

	@Bean
	fun jwtDecoder(): JwtDecoder =
		NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()
}
