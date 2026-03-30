package com.soat.tech.challenge.oficina.application.api

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
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class MappersTest {

	@Test
	fun `mapeia entidades para response`() {
		val cid = UUID.randomUUID()
		val vid = UUID.randomUUID()
		val sid = UUID.randomUUID()
		val pid = UUID.randomUUID()
		val doc = DocumentoFiscal.parse("52998224725")
		assertEquals("Ana", Cliente(cid, doc, "Ana").toResponse().nome)
		assertEquals("ABC1234", Veiculo(vid, cid, PlacaVeiculo.parse("ABC1234"), "F", "M", 2020).toResponse().placa)
		assertEquals(100, ServicoCatalogo(sid, "S", "d", 100, 10).toResponse().precoCentavos)
		assertEquals(3, Peca(pid, "C", "N", 50, 3).toResponse().quantidadeEstoque)
		val os = OrdemServico(
			id = UUID.randomUUID(),
			codigoAcompanhamento = "cod",
			clienteId = cid,
			veiculoId = vid,
			status = StatusOrdemServico.RECEBIDA,
			linhasServico = mutableListOf(LinhaServicoOrdem(sid, 1, 100)),
			linhasPeca = mutableListOf(LinhaPecaOrdem(pid, 1, 50)),
			valorServicosCentavos = 100,
			valorPecasCentavos = 50,
			valorTotalCentavos = 150,
		)
		val dto = os.toResponse({ "Srv" }, { "Pec" })
		assertEquals(150, dto.valorTotalCentavos)
		assertEquals("Srv", dto.servicos.single().nomeServico)
		assertEquals("Pec", dto.pecas.single().nomePeca)
	}
}
