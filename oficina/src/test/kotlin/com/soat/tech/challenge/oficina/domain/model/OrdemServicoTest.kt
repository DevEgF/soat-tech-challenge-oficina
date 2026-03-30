package com.soat.tech.challenge.oficina.domain.model

import com.soat.tech.challenge.oficina.domain.exception.TransicaoStatusInvalidaException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import java.time.Instant
import java.util.UUID

class OrdemServicoTest {

	private val t0 = Instant.parse("2026-01-01T10:00:00Z")

	@Test
	fun `fluxo feliz de estados`() {
		val os = OrdemServico.nova(
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = listOf(
				LinhaServicoOrdem(UUID.randomUUID(), 1, 5000),
			),
			linhasPeca = emptyList(),
		)
		assertEquals(StatusOrdemServico.RECEBIDA, os.status)
		os.iniciarDiagnostico(t0)
		assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, os.status)
		os.enviarOrcamento(t0.plusSeconds(60))
		assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, os.status)
		os.aprovarOrcamento(t0.plusSeconds(120))
		assertEquals(StatusOrdemServico.EM_EXECUCAO, os.status)
		os.concluirServicos(t0.plusSeconds(180))
		assertEquals(StatusOrdemServico.FINALIZADA, os.status)
		os.registrarEntrega(t0.plusSeconds(240))
		assertEquals(StatusOrdemServico.ENTREGUE, os.status)
	}

	@Test
	fun `nao permite enviar orcamento sem diagnostico`() {
		val os = OrdemServico.nova(
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		assertFailsWith<TransicaoStatusInvalidaException> {
			os.enviarOrcamento(t0)
		}
	}

	@Test
	fun `recalcula totais`() {
		val sid = UUID.randomUUID()
		val pid = UUID.randomUUID()
		val os = OrdemServico.nova(
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = listOf(LinhaServicoOrdem(sid, 2, 1000)),
			linhasPeca = listOf(LinhaPecaOrdem(pid, 3, 500)),
		)
		assertEquals(2000L, os.valorServicosCentavos)
		assertEquals(1500L, os.valorPecasCentavos)
		assertEquals(3500L, os.valorTotalCentavos)
	}
}
