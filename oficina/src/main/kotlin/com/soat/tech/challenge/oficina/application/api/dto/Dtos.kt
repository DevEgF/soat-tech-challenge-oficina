package com.soat.tech.challenge.oficina.application.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Corpo de login administrativo")
data class LoginRequest(
	@field:NotBlank val username: String = "",
	@field:NotBlank val password: String = "",
)

data class LoginResponse(val accessToken: String, val tokenType: String = "Bearer", val expiresInSeconds: Long)

data class ClienteRequest(
	@get:JsonProperty("documento")
	@field:NotBlank val taxIdDigits: String = "",
	@get:JsonProperty("nome")
	@field:NotBlank val name: String = "",
	@get:JsonProperty("email")
	val email: String? = null,
	@get:JsonProperty("telefone")
	val phone: String? = null,
)

data class ClienteResponse(
	val id: UUID,
	@get:JsonProperty("documento") val taxIdDigits: String,
	@get:JsonProperty("nome") val name: String,
	@get:JsonProperty("email") val email: String?,
	@get:JsonProperty("telefone") val phone: String?,
)

data class VeiculoRequest(
	@get:JsonProperty("clienteId")
	@field:NotNull val customerId: UUID? = null,
	@get:JsonProperty("placa")
	@field:NotBlank val plate: String = "",
	@get:JsonProperty("marca")
	@field:NotBlank val brand: String = "",
	@get:JsonProperty("modelo")
	@field:NotBlank val model: String = "",
	@get:JsonProperty("ano")
	@field:NotNull @field:Min(1900) val year: Int? = null,
)

data class VeiculoResponse(
	val id: UUID,
	@get:JsonProperty("clienteId") val customerId: UUID,
	@get:JsonProperty("placa") val plate: String,
	@get:JsonProperty("marca") val brand: String,
	@get:JsonProperty("modelo") val model: String,
	@get:JsonProperty("ano") val year: Int,
)

data class ServicoCatalogoRequest(
	@get:JsonProperty("nome")
	@field:NotBlank val name: String = "",
	@get:JsonProperty("descricao")
	val description: String? = null,
	@get:JsonProperty("precoCentavos")
	@field:NotNull @field:Min(0) val priceCents: Long? = null,
	@get:JsonProperty("tempoEstimadoMinutos")
	@field:NotNull @field:Min(1) val estimatedMinutes: Int? = null,
)

data class ServicoCatalogoResponse(
	val id: UUID,
	@get:JsonProperty("nome") val name: String,
	@get:JsonProperty("descricao") val description: String?,
	@get:JsonProperty("precoCentavos") val priceCents: Long,
	@get:JsonProperty("tempoEstimadoMinutos") val estimatedMinutes: Int,
)

data class PecaRequest(
	@get:JsonProperty("codigo")
	@field:NotBlank val code: String = "",
	@get:JsonProperty("nome")
	@field:NotBlank val name: String = "",
	@get:JsonProperty("precoCentavos")
	@field:NotNull @field:Min(0) val priceCents: Long? = null,
	@get:JsonProperty("quantidadeEstoque")
	@field:NotNull @field:Min(0) val stockQuantity: Int? = null,
	@get:JsonProperty("pontoReposicao")
	@field:Min(0) val replenishmentPoint: Int? = null,
)

data class PecaResponse(
	val id: UUID,
	@get:JsonProperty("codigo") val code: String,
	@get:JsonProperty("nome") val name: String,
	@get:JsonProperty("precoCentavos") val priceCents: Long,
	@get:JsonProperty("quantidadeEstoque") val stockQuantity: Int,
	@get:JsonProperty("pontoReposicao") val replenishmentPoint: Int?,
)

data class EntradaMercadoriaRequest(
	@get:JsonProperty("quantidade")
	@field:NotNull @field:Min(1) val quantity: Int? = null,
	@get:JsonProperty("referencia")
	val reference: String? = null,
)

data class OrdemServicoLinhaServicoRequest(
	@get:JsonProperty("servicoCatalogoId")
	@field:NotNull val catalogServiceId: UUID? = null,
	@get:JsonProperty("quantidade")
	@field:NotNull @field:Min(1) val quantity: Int? = null,
)

data class OrdemServicoLinhaPecaRequest(
	@get:JsonProperty("pecaId")
	@field:NotNull val partId: UUID? = null,
	@get:JsonProperty("quantidade")
	@field:NotNull @field:Min(1) val quantity: Int? = null,
)

data class CriarOrdemServicoRequest(
	@get:JsonProperty("documentoCliente")
	@field:NotBlank val customerTaxIdDigits: String = "",
	@get:JsonProperty("nomeCliente")
	@field:NotBlank val customerName: String = "",
	@get:JsonProperty("emailCliente")
	val customerEmail: String? = null,
	@get:JsonProperty("telefoneCliente")
	val customerPhone: String? = null,
	@get:JsonProperty("placa")
	@field:NotBlank val plate: String = "",
	@get:JsonProperty("marca")
	@field:NotBlank val vehicleBrand: String = "",
	@get:JsonProperty("modelo")
	@field:NotBlank val vehicleModel: String = "",
	@get:JsonProperty("anoVeiculo")
	@field:NotNull @field:Min(1900) val vehicleYear: Int? = null,
	@get:JsonProperty("servicos")
	val services: List<OrdemServicoLinhaServicoRequest> = emptyList(),
	@get:JsonProperty("pecas")
	val parts: List<OrdemServicoLinhaPecaRequest> = emptyList(),
)

data class OrdemServicoLinhaServicoResponse(
	@get:JsonProperty("servicoCatalogoId") val catalogServiceId: UUID,
	@get:JsonProperty("nomeServico") val serviceName: String?,
	@get:JsonProperty("quantidade") val quantity: Int,
	@get:JsonProperty("precoUnitarioCentavos") val unitPriceCents: Long,
)

data class OrdemServicoLinhaPecaResponse(
	@get:JsonProperty("pecaId") val partId: UUID,
	@get:JsonProperty("nomePeca") val partName: String?,
	@get:JsonProperty("quantidade") val quantity: Int,
	@get:JsonProperty("precoUnitarioCentavos") val unitPriceCents: Long,
)

data class OrdemServicoResponse(
	val id: UUID,
	@get:JsonProperty("codigoAcompanhamento") val trackingCode: String,
	@get:JsonProperty("clienteId") val customerId: UUID,
	@get:JsonProperty("veiculoId") val vehicleId: UUID,
	val status: StatusOrdemServico,
	@get:JsonProperty("valorServicosCentavos") val servicesTotalCents: Long,
	@get:JsonProperty("valorPecasCentavos") val partsTotalCents: Long,
	@get:JsonProperty("valorTotalCentavos") val totalCents: Long,
	@get:JsonProperty("servicos") val services: List<OrdemServicoLinhaServicoResponse>,
	@get:JsonProperty("pecas") val parts: List<OrdemServicoLinhaPecaResponse>,
)

data class AcompanhamentoOsResponse(
	@get:JsonProperty("codigoAcompanhamento") val trackingCode: String,
	val status: StatusOrdemServico,
	@get:JsonProperty("valorTotalCentavos") val totalCents: Long,
	@get:JsonProperty("placaVeiculo") val vehiclePlate: String,
	@get:JsonProperty("documentoClienteMascarado") val maskedCustomerTaxId: String,
)

data class TempoMedioServicoResponse(
	@get:JsonProperty("servicoCatalogoId") val catalogServiceId: UUID,
	@get:JsonProperty("nomeServico") val serviceName: String,
	@get:JsonProperty("mediaMinutos") val averageMinutes: Double,
	@get:JsonProperty("amostras") val sampleCount: Long,
)

data class ReservaPecaOsResponse(
	val id: UUID,
	@get:JsonProperty("ordemServicoId") val workOrderId: UUID,
	@get:JsonProperty("pecaId") val partId: UUID,
	@get:JsonProperty("nomePeca") val partName: String,
	val quantity: Int,
	val status: String,
)

data class EstoqueAlertaResponse(
	@get:JsonProperty("pecaId") val partId: UUID,
	val code: String,
	val name: String,
	@get:JsonProperty("quantidadeEstoque") val stockQuantity: Int,
	@get:JsonProperty("pontoReposicao") val replenishmentPoint: Int?,
	@get:JsonProperty("quantidadeReservadaPendente") val pendingReservedQuantity: Int,
)
