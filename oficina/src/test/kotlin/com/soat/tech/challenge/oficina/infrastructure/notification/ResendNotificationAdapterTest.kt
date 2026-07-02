package com.soat.tech.challenge.oficina.infrastructure.notification

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

/**
 * Task 4 / Task 9: falha no envio de notificação não pode quebrar o fluxo de negócio.
 * Aponta para um host que recusa conexão (porta fechada em localhost) para forçar
 * a falha de rede sem depender de serviço externo real.
 */
@DisplayName("ResendNotificationAdapter — tolerância a falha de envio")
class ResendNotificationAdapterTest {

    private val unreachableBaseUrl = "http://127.0.0.1:1"

    private fun adapter(): ResendNotificationAdapter =
        ResendNotificationAdapter(
            apiKey = "fake-api-key",
            fromEmail = "noreply@oficinasys.local",
            baseUrl = unreachableBaseUrl,
        )

    @Test
    @DisplayName("notifyWorkOrderFinalized não propaga exceção quando o envio falha")
    fun finalizedNotificationDoesNotThrowOnFailure() {
        assertDoesNotThrow {
            adapter().notifyWorkOrderFinalized(
                customerName = "Cliente Teste",
                customerEmail = "cliente@example.com",
                vehicleModel = "Gol",
            )
        }
    }

    @Test
    @DisplayName("notifyQuoteSentToCustomer não propaga exceção quando o envio falha")
    fun quoteSentNotificationDoesNotThrowOnFailure() {
        assertDoesNotThrow {
            adapter().notifyQuoteSentToCustomer(
                customerName = "Cliente Teste",
                customerEmail = "cliente@example.com",
                vehicleModel = "Gol",
                quoteTotalCents = 15000,
            )
        }
    }

    @Test
    @DisplayName("notifyDeliveryRegistered não propaga exceção quando o envio falha")
    fun deliveryRegisteredNotificationDoesNotThrowOnFailure() {
        assertDoesNotThrow {
            adapter().notifyDeliveryRegistered(
                customerName = "Cliente Teste",
                customerEmail = "cliente@example.com",
                vehicleModel = "Gol",
            )
        }
    }
}
