package com.soat.tech.challenge.oficina.application.api

import com.soat.tech.challenge.oficina.application.api.dto.ClienteResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaPecaResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaServicoResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import com.soat.tech.challenge.oficina.application.api.dto.PecaResponse
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoResponse
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import java.util.UUID

fun Cliente.toResponse(): ClienteResponse = ClienteResponse(
	id = id,
	documento = documento.digitos,
	nome = nome,
	email = email,
	telefone = telefone,
)

fun Veiculo.toResponse(): VeiculoResponse = VeiculoResponse(
	id = id,
	clienteId = clienteId,
	placa = placa.normalizada,
	marca = marca,
	modelo = modelo,
	ano = ano,
)

fun ServicoCatalogo.toResponse(): ServicoCatalogoResponse = ServicoCatalogoResponse(
	id = id,
	nome = nome,
	descricao = descricao,
	precoCentavos = precoCentavos,
	tempoEstimadoMinutos = tempoEstimadoMinutos,
)

fun Peca.toResponse(): PecaResponse = PecaResponse(
	id = id,
	codigo = codigo,
	nome = nome,
	precoCentavos = precoCentavos,
	quantidadeEstoque = quantidadeEstoque,
)

fun OrdemServico.toResponse(
	nomeServico: (UUID) -> String? = { null },
	nomePeca: (UUID) -> String? = { null },
): OrdemServicoResponse = OrdemServicoResponse(
	id = id,
	codigoAcompanhamento = codigoAcompanhamento,
	clienteId = clienteId,
	veiculoId = veiculoId,
	status = status,
	valorServicosCentavos = valorServicosCentavos,
	valorPecasCentavos = valorPecasCentavos,
	valorTotalCentavos = valorTotalCentavos,
	servicos = linhasServico.map {
		OrdemServicoLinhaServicoResponse(
			servicoCatalogoId = it.servicoCatalogoId,
			nomeServico = nomeServico(it.servicoCatalogoId),
			quantidade = it.quantidade,
			precoUnitarioCentavos = it.precoUnitarioCentavos,
		)
	},
	pecas = linhasPeca.map {
		OrdemServicoLinhaPecaResponse(
			pecaId = it.pecaId,
			nomePeca = nomePeca(it.pecaId),
			quantidade = it.quantidade,
			precoUnitarioCentavos = it.precoUnitarioCentavos,
		)
	},
)
