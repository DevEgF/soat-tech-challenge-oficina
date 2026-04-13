package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.VehicleRequest
import com.soat.tech.challenge.oficina.domain.exception.BusinessRuleException
import com.soat.tech.challenge.oficina.domain.model.LicensePlate
import com.soat.tech.challenge.oficina.domain.model.Vehicle
import com.soat.tech.challenge.oficina.domain.port.CustomerRepository
import com.soat.tech.challenge.oficina.domain.port.VehicleRepository
import io.mockk.every
import io.mockk.mockk
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class VehicleApplicationServiceTest {

	private val vehicles = mockk<VehicleRepository>()
	private val customers = mockk<CustomerRepository>()
	private val service = VehicleApplicationService(vehicles, customers)

	@Nested
	@DisplayName("Given customer exists and plate free")
	inner class GivenValidCreate {

		@Test
		@DisplayName("when create then saves vehicle")
		fun create() {
			val cid = UUID.randomUUID()
			every { customers.findById(cid) } returns Optional.of(mockk(relaxed = true))
			every { vehicles.findByLicensePlate(LicensePlate.parse("ABC1234")) } returns Optional.empty()
			every { vehicles.save(any()) } answers { firstArg() }
			val r = service.create(
				VehicleRequest(customerId = cid, plate = "ABC1234", brand = "F", model = "X", year = 2020),
			)
			assertEquals("ABC1234", r.plate)
		}
	}

	@Nested
	@DisplayName("Given plate already taken")
	inner class GivenDuplicatePlate {

		@Test
		@DisplayName("when create then throws")
		fun duplicate() {
			val cid = UUID.randomUUID()
			every { customers.findById(cid) } returns Optional.of(mockk(relaxed = true))
			every { vehicles.findByLicensePlate(LicensePlate.parse("ABC1234")) } returns Optional.of(
				Vehicle(UUID.randomUUID(), cid, LicensePlate.parse("ABC1234"), "F", "X", 2020),
			)
			assertFailsWith<BusinessRuleException> {
				service.create(VehicleRequest(customerId = cid, plate = "ABC1234", brand = "F", model = "X", year = 2020))
			}
		}
	}

	@Test
	@DisplayName("when list empty then zero")
	fun listEmpty() {
		every { vehicles.findAll() } returns emptyList()
		assertEquals(0, service.list().size)
	}
}
