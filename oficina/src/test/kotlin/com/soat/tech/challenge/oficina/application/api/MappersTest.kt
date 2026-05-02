package com.soat.tech.challenge.oficina.application.api

import com.soat.tech.challenge.oficina.domain.model.Customer
import com.soat.tech.challenge.oficina.domain.model.TaxDocument
import com.soat.tech.challenge.oficina.domain.model.PartLine
import com.soat.tech.challenge.oficina.domain.model.ServiceLine
import com.soat.tech.challenge.oficina.domain.model.WorkOrder
import com.soat.tech.challenge.oficina.domain.model.Part
import com.soat.tech.challenge.oficina.domain.model.LicensePlate
import com.soat.tech.challenge.oficina.domain.model.CatalogService
import com.soat.tech.challenge.oficina.domain.model.WorkOrderStatus
import com.soat.tech.challenge.oficina.domain.model.Vehicle
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
			val doc = TaxDocument.parse("52998224725")
			assertEquals("Ana", Customer(cid, doc, "Ana").toResponse().name)
			assertEquals("ABC1234", Vehicle(vid, cid, LicensePlate.parse("ABC1234"), "F", "M", 2020).toResponse().plate)
			assertEquals(100, CatalogService(sid, "S", "d", 100, 10).toResponse().priceCents)
			assertEquals(3, Part(pid, "C", "N", 50, 3).toResponse().stockQuantity)
			val wo = WorkOrder(
				id = UUID.randomUUID(),
				trackingCode = "cod",
				customerId = cid,
				vehicleId = vid,
				status = WorkOrderStatus.RECEIVED,
				serviceLines = mutableListOf(ServiceLine(sid, 1, 100)),
				partLines = mutableListOf(PartLine(pid, 1, 50)),
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
