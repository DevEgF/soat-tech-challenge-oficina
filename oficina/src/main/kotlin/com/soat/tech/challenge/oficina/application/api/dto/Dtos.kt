package com.soat.tech.challenge.oficina.application.api.dto

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
	@field:NotBlank val documento: String = "",
	@field:NotBlank val nome: String = "",
	val email: String? = null,
	val telefone: String? = null,
)

data class ClienteResponse(
	val id: UUID,
	val documento: String,
	val nome: String,
	val email: String?,
	val telefone: String?,
)

data class VeiculoRequest(
	@field:NotNull val clienteId: UUID? = null,
	@field:NotBlank val placa: String = "",
	@field:NotBlank val marca: String = "",
	@field:NotBlank val modelo: String = "",
	@field:NotNull @field:Min(1900) val ano: Int? = null,
)

data class VeiculoResponse(
	val id: UUID,
	val clienteId: UUID,
	val placa: String,
	val marca: String,
	val modelo: String,
	val ano: Int,
)

data class ServicoCatalogoRequest(
	@field:NotBlank val nome: String = "",
	val descricao: String? = null,
	@field:NotNull @field:Min(0) val precoCentavos: Long? = null,
	@field:NotNull @field:Min(1) val tempoEstimadoMinutos: Int? = null,
)

data class ServicoCatalogoResponse(
	val id: UUID,
	val nome: String,
	val descricao: String?,
	val precoCentavos: Long,
	val tempoEstimadoMinutos: Int,
)

data class PecaRequest(
	@field:NotBlank val codigo: String = "",
	@field:NotBlank val nome: String = "",
	@field:NotNull @field:Min(0) val precoCentavos: Long? = null,
	@field:NotNull @field:Min(0) val quantidadeEstoque: Int? = null,
)

data class PecaResponse(
	val id: UUID,
	val codigo: String,
	val nome: String,
	val precoCentavos: Long,
	val quantidadeEstoque: Int,
)

data class OrdemServicoLinhaServicoRequest(
	@field:NotNull val servicoCatalogoId: UUID? = null,
	@field:NotNull @field:Min(1) val quantidade: Int? = null,
)

data class OrdemServicoLinhaPecaRequest(
	@field:NotNull val pecaId: UUID? = null,
	@field:NotNull @field:Min(1) val quantidade: Int? = null,
)

data class CriarOrdemServicoRequest(
	@field:NotBlank val documentoCliente: String = "",
	@field:NotBlank val nomeCliente: String = "",
	val emailCliente: String? = null,
	val telefoneCliente: String? = null,
	@field:NotBlank val placa: String = "",
	@field:NotBlank val marca: String = "",
	@field:NotBlank val modelo: String = "",
	@field:NotNull @field:Min(1900) val anoVeiculo: Int? = null,
	val servicos: List<OrdemServicoLinhaServicoRequest> = emptyList(),
	val pecas: List<OrdemServicoLinhaPecaRequest> = emptyList(),
)

data class OrdemServicoLinhaServicoResponse(
	val servicoCatalogoId: UUID,
	val nomeServico: String?,
	val quantidade: Int,
	val precoUnitarioCentavos: Long,
)

data class OrdemServicoLinhaPecaResponse(
	val pecaId: UUID,
	val nomePeca: String?,
	val quantidade: Int,
	val precoUnitarioCentavos: Long,
)

data class OrdemServicoResponse(
	val id: UUID,
	val codigoAcompanhamento: String,
	val clienteId: UUID,
	val veiculoId: UUID,
	val status: StatusOrdemServico,
	val valorServicosCentavos: Long,
	val valorPecasCentavos: Long,
	val valorTotalCentavos: Long,
	val servicos: List<OrdemServicoLinhaServicoResponse>,
	val pecas: List<OrdemServicoLinhaPecaResponse>,
)

data class AcompanhamentoOsResponse(
	val codigoAcompanhamento: String,
	val status: StatusOrdemServico,
	val valorTotalCentavos: Long,
	val placaVeiculo: String,
	val documentoClienteMascarado: String,
)

data class TempoMedioServicoResponse(
	val servicoCatalogoId: UUID,
	val nomeServico: String,
	val mediaMinutos: Double,
	val amostras: Long,
)
