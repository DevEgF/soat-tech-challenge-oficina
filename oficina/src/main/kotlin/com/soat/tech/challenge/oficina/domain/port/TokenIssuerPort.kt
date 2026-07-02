package com.soat.tech.challenge.oficina.domain.port

/** Token emitido para um usuário autenticado e seu tempo de expiração em segundos. */
data class IssuedToken(val token: String, val expiresInSeconds: Long)

/**
 * Porta de saída para emissão de tokens de acesso.
 * A implementação técnica (JWT/assinatura) vive na infraestrutura.
 */
interface TokenIssuerPort {
	fun issueForUser(username: String): IssuedToken
}
