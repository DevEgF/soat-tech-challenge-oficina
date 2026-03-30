package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.TransicaoStatusInvalidaException
import java.time.Instant
import java.util.UUID

data class LinhaServicoOrdem(
	val servicoCatalogoId: UUID,
	val quantidade: Int,
	val precoUnitarioCentavos: Long,
) {
	init {
		require(quantidade > 0) { "Quantidade de serviço deve ser positiva" }
		require(precoUnitarioCentavos >= 0) { "Preço não pode ser negativo" }
	}
}

data class LinhaPecaOrdem(
	val pecaId: UUID,
	val quantidade: Int,
	val precoUnitarioCentavos: Long,
) {
	init {
		require(quantidade > 0) { "Quantidade de peça deve ser positiva" }
		require(precoUnitarioCentavos >= 0) { "Preço não pode ser negativo" }
	}
}

/**
 * Ordem de serviço (agregado): orçamento derivado de linhas; máquina de estados explícita.
 */
data class OrdemServico(
	val id: UUID,
	val codigoAcompanhamento: String,
	val clienteId: UUID,
	val veiculoId: UUID,
	var status: StatusOrdemServico,
	val linhasServico: MutableList<LinhaServicoOrdem>,
	val linhasPeca: MutableList<LinhaPecaOrdem>,
	var valorServicosCentavos: Long,
	var valorPecasCentavos: Long,
	var valorTotalCentavos: Long,
	var diagnosticadoEm: Instant? = null,
	var orcamentoEnviadoEm: Instant? = null,
	var aprovadoEm: Instant? = null,
	var execucaoIniciadaEm: Instant? = null,
	var finalizadaEm: Instant? = null,
	var entregueEm: Instant? = null,
) {

	fun recalcularOrcamento() {
		valorServicosCentavos = linhasServico.sumOf { it.quantidade.toLong() * it.precoUnitarioCentavos }
		valorPecasCentavos = linhasPeca.sumOf { it.quantidade.toLong() * it.precoUnitarioCentavos }
		valorTotalCentavos = valorServicosCentavos + valorPecasCentavos
	}

	private fun exigeStatus(esperado: StatusOrdemServico, acao: String) {
		if (status != esperado) {
			throw TransicaoStatusInvalidaException(status.name, acao)
		}
	}

	fun iniciarDiagnostico(agora: Instant) {
		exigeStatus(StatusOrdemServico.RECEBIDA, "iniciarDiagnostico")
		status = StatusOrdemServico.EM_DIAGNOSTICO
		diagnosticadoEm = agora
	}

	fun enviarOrcamento(agora: Instant) {
		exigeStatus(StatusOrdemServico.EM_DIAGNOSTICO, "enviarOrcamento")
		recalcularOrcamento()
		status = StatusOrdemServico.AGUARDANDO_APROVACAO
		orcamentoEnviadoEm = agora
	}

	fun aprovarOrcamento(agora: Instant) {
		exigeStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "aprovarOrcamento")
		status = StatusOrdemServico.EM_EXECUCAO
		aprovadoEm = agora
		execucaoIniciadaEm = agora
	}

	fun voltarParaDiagnostico(agora: Instant) {
		exigeStatus(StatusOrdemServico.AGUARDANDO_APROVACAO, "voltarParaDiagnostico")
		status = StatusOrdemServico.EM_DIAGNOSTICO
		orcamentoEnviadoEm = null
	}

	fun concluirServicos(agora: Instant) {
		exigeStatus(StatusOrdemServico.EM_EXECUCAO, "concluirServicos")
		status = StatusOrdemServico.FINALIZADA
		finalizadaEm = agora
	}

	fun registrarEntrega(agora: Instant) {
		exigeStatus(StatusOrdemServico.FINALIZADA, "registrarEntrega")
		status = StatusOrdemServico.ENTREGUE
		entregueEm = agora
	}

	companion object {
		fun nova(
			id: UUID = UUID.randomUUID(),
			codigoAcompanhamento: String = UUID.randomUUID().toString(),
			clienteId: UUID,
			veiculoId: UUID,
			linhasServico: List<LinhaServicoOrdem>,
			linhasPeca: List<LinhaPecaOrdem>,
		): OrdemServico {
			val os = OrdemServico(
				id = id,
				codigoAcompanhamento = codigoAcompanhamento,
				clienteId = clienteId,
				veiculoId = veiculoId,
				status = StatusOrdemServico.RECEBIDA,
				linhasServico = linhasServico.toMutableList(),
				linhasPeca = linhasPeca.toMutableList(),
				valorServicosCentavos = 0,
				valorPecasCentavos = 0,
				valorTotalCentavos = 0,
			)
			os.recalcularOrcamento()
			return os
		}
	}
}
