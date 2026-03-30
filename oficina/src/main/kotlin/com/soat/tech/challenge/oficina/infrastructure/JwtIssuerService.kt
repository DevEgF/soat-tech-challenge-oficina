package com.soat.tech.challenge.oficina.infrastructure

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtIssuerService(
	private val jwtSigningKey: SecretKey,
	@Value("\${app.jwt.expiration-minutes:60}") private val expirationMinutes: Long,
) {

	fun issueForUser(username: String): Pair<String, Long> {
		val now = Instant.now()
		val exp = now.plus(expirationMinutes, ChronoUnit.MINUTES)
		val claims = JWTClaimsSet.Builder()
			.issuer("oficina")
			.issueTime(Date.from(now))
			.expirationTime(Date.from(exp))
			.subject(username)
			.claim("scope", listOf("ADMIN"))
			.build()
		val header = JWSHeader(JWSAlgorithm.HS256)
		val jwt = SignedJWT(header, claims)
		val signer = MACSigner(jwtSigningKey.encoded)
		jwt.sign(signer)
		val seconds = ChronoUnit.SECONDS.between(now, exp)
		return Pair(jwt.serialize(), seconds)
	}
}
