package com.soat.tech.challenge.oficina.application

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ResendEmailService(
	@Value("\${app.resend.api-key:}") private val apiKey: String,
	@Value("\${app.resend.from-email:noreply@oficinasys.local}") private val fromEmail: String,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun sendWorkOrderFinalizedEmail(
		customerName: String,
		customerEmail: String?,
		vehicleModel: String,
	) {
		if (customerEmail.isNullOrBlank()) {
			log.info("Skipping finalization email: customer has no email")
			return
		}
		if (apiKey.isBlank()) {
			log.info("Skipping finalization email: APP_RESEND_API_KEY not configured")
			return
		}

		val subject = "Seu veiculo esta pronto para retirada"
		val htmlBody = """
			<p>Ola, ${escapeHtml(customerName)}!</p>
			<p>Seu veiculo <strong>${escapeHtml(vehicleModel)}</strong> teve os servicos concluidos e ja esta disponivel para retirada.</p>
			<p>Se precisar, nossa equipe esta a disposicao para qualquer duvida.</p>
			<p>Obrigado por confiar na OficinaSys.</p>
		""".trimIndent()

		val payload = mapOf(
			"from" to fromEmail,
			"to" to listOf(customerEmail),
			"subject" to subject,
			"html" to htmlBody,
		)

		try {
			RestClient.builder()
				.baseUrl("https://api.resend.com")
				.defaultHeader("Authorization", "Bearer $apiKey")
				.build()
				.post()
				.uri("/emails")
				.contentType(MediaType.APPLICATION_JSON)
				.body(payload)
				.retrieve()
				.toBodilessEntity()
			log.info("Finalization email sent to {}", customerEmail)
		} catch (ex: Exception) {
			log.warn("Failed to send finalization email to {}: {}", customerEmail, ex.message)
		}
	}

	private fun escapeHtml(value: String): String =
		value
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;")
}
