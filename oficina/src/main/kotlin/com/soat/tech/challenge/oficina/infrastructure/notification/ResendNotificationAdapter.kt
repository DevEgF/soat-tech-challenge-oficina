package com.soat.tech.challenge.oficina.infrastructure.notification

import com.soat.tech.challenge.oficina.domain.port.NotificationPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ResendNotificationAdapter(
    @Value("\${app.resend.api-key:}") private val apiKey: String,
    @Value("\${app.resend.from-email:noreply@oficinasys.local}") private val fromEmail: String,
    @Value("\${app.resend.base-url:https://api.resend.com}") private val baseUrl: String,
) : NotificationPort {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun notifyWorkOrderFinalized(customerName: String, customerEmail: String?, vehicleModel: String) {
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
                .baseUrl(baseUrl)
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

    override fun notifyQuoteSentToCustomer(customerName: String, customerEmail: String?, vehicleModel: String, quoteTotalCents: Long) {
        if (customerEmail.isNullOrBlank()) {
            log.info("Skipping quote sent email: customer has no email")
            return
        }
        if (apiKey.isBlank()) {
            log.info("Skipping quote sent email: APP_RESEND_API_KEY not configured")
            return
        }

        val subject = "Orçamento enviado para aprovação"
        val totalInReais = String.format("%.2f", quoteTotalCents / 100.0)
        val htmlBody = """
            <p>Ola, ${escapeHtml(customerName)}!</p>
            <p>Seu veiculo <strong>${escapeHtml(vehicleModel)}</strong> está com orçamento de R$${totalInReais} disponível para aprovação.</p>
            <p>Por favor, acesse o sistema para verificar e aprovar ou rejeitar o orçamento.</p>
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
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer $apiKey")
                .build()
                .post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
            log.info("Quote sent email sent to {}", customerEmail)
        } catch (ex: Exception) {
            log.warn("Failed to send quote sent email to {}: {}", customerEmail, ex.message)
        }
    }

    override fun notifyDeliveryRegistered(customerName: String, customerEmail: String?, vehicleModel: String) {
        if (customerEmail.isNullOrBlank()) {
            log.info("Skipping delivery registered email: customer has no email")
            return
        }
        if (apiKey.isBlank()) {
            log.info("Skipping delivery registered email: APP_RESEND_API_KEY not configured")
            return
        }

        val subject = "Veiculo entregue"
        val htmlBody = """
            <p>Ola, ${escapeHtml(customerName)}!</p>
            <p>Seu veiculo <strong>${escapeHtml(vehicleModel)}</strong> foi entregue com sucesso.</p>
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
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer $apiKey")
                .build()
                .post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
            log.info("Delivery registered email sent to {}", customerEmail)
        } catch (ex: Exception) {
            log.warn("Failed to send delivery registered email to {}: {}", customerEmail, ex.message)
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