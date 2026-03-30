package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.LinhaServicoOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ClienteEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PecaEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ServicoCatalogoEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VeiculoEntity
import java.util.UUID

fun ClienteEntity.toDomain(): Cliente = Cliente(
	id = UUID.fromString(id),
	documento = DocumentoFiscal.parse(documento),
	nome = nome,
	email = email,
	telefone = telefone,
)

fun Cliente.toEntity(): ClienteEntity = ClienteEntity(
	id = id.toString(),
	documento = documento.digitos,
	nome = nome,
	email = email,
	telefone = telefone,
)

fun VeiculoEntity.toDomain(): Veiculo = Veiculo(
	id = UUID.fromString(id),
	clienteId = UUID.fromString(cliente!!.id),
	placa = PlacaVeiculo.parse(placa),
	marca = marca,
	modelo = modelo,
	ano = ano,
)

fun ServicoCatalogoEntity.toDomain(): ServicoCatalogo = ServicoCatalogo(
	id = UUID.fromString(id),
	nome = nome,
	descricao = descricao,
	precoCentavos = precoCentavos,
	tempoEstimadoMinutos = tempoEstimadoMinutos,
)

fun PecaEntity.toDomain(): Peca = Peca(
	id = UUID.fromString(id),
	codigo = codigo,
	nome = nome,
	precoCentavos = precoCentavos,
	quantidadeEstoque = quantidadeEstoque,
)

fun OrdemServicoEntity.toDomain(): OrdemServico {
	val linhasS = linhasServico.toList().map {
		LinhaServicoOrdem(
			servicoCatalogoId = UUID.fromString(it.servicoCatalogo!!.id),
			quantidade = it.quantidade,
			precoUnitarioCentavos = it.precoUnitarioCentavos,
		)
	}.toMutableList()
	val linhasP = linhasPeca.toList().map {
		LinhaPecaOrdem(
			pecaId = UUID.fromString(it.peca!!.id),
			quantidade = it.quantidade,
			precoUnitarioCentavos = it.precoUnitarioCentavos,
		)
	}.toMutableList()
	return OrdemServico(
		id = UUID.fromString(id),
		codigoAcompanhamento = codigoAcompanhamento,
		clienteId = UUID.fromString(cliente!!.id),
		veiculoId = UUID.fromString(veiculo!!.id),
		status = StatusOrdemServico.valueOf(status),
		linhasServico = linhasS,
		linhasPeca = linhasP,
		valorServicosCentavos = valorServicosCentavos,
		valorPecasCentavos = valorPecasCentavos,
		valorTotalCentavos = valorTotalCentavos,
		diagnosticadoEm = diagnosticadoEm,
		orcamentoEnviadoEm = orcamentoEnviadoEm,
		aprovadoEm = aprovadoEm,
		execucaoIniciadaEm = execucaoIniciadaEm,
		finalizadaEm = finalizadaEm,
		entregueEm = entregueEm,
	)
}
