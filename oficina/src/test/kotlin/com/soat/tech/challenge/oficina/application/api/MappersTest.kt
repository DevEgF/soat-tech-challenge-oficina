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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals

class MappersTest {

	@Nested
	@DisplayName("Given domain aggregates")
	inner class GivenDomainAggregates {

		@Test
		@DisplayName("when mapping to API responses then fields match")
		fun mapsToResponses() {
			val cid = UUID.randomUUID()
			val vid = UUID.randomUUID()
			val sid = UUID.randomUUID()
			val pid = UUID.randomUUID()
			val doc = DocumentoFiscal.parse("52998224725")
			assertEquals("Ana", Cliente(cid, doc, "Ana").toResponse().name)
			assertEquals("ABC1234", Veiculo(vid, cid, PlacaVeiculo.parse("ABC1234"), "F", "M", 2020).toResponse().plate)
			assertEquals(100, ServicoCatalogo(sid, "S", "d", 100, 10).toResponse().priceCents)
			assertEquals(3, Peca(pid, "C", "N", 50, 3).toResponse().stockQuantity)
			val wo = OrdemServico(
				id = UUID.randomUUID(),
				trackingCode = "cod",
				customerId = cid,
				vehicleId = vid,
				status = StatusOrdemServico.RECEBIDA,
				serviceLines = mutableListOf(LinhaServicoOrdem(sid, 1, 100)),
				partLines = mutableListOf(LinhaPecaOrdem(pid, 1, 50)),
				servicesTotalCents = 100,
				partsTotalCents = 50,
				totalCents = 150,
			)
			val dto = wo.toResponse({ "Srv" }, { "Pec" })
			assertEquals(150, dto.totalCents)
			assertEquals("Srv", dto.services.single().serviceName)
			assertEquals("Pec", dto.parts.single().partName)
		}
	}
}
