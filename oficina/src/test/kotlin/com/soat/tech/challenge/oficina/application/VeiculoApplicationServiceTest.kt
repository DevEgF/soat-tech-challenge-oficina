package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.VeiculoRequest
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VeiculoApplicationServiceTest {

	private val veiculos = mockk<VeiculoRepository>()
	private val clientes = mockk<ClienteRepository>()
	private val service = VeiculoApplicationService(veiculos, clientes)

	@Test
	fun `criar veiculo`() {
		val cid = UUID.randomUUID()
		every { clientes.findById(cid) } returns Optional.of(
			Cliente(cid, DocumentoFiscal.parse("52998224725"), "A"),
		)
		every { veiculos.findByPlaca(PlacaVeiculo.parse("ABC1234")) } returns Optional.empty()
		every { veiculos.save(any()) } answers { firstArg() }
		val r = service.criar(
			VeiculoRequest(clienteId = cid, placa = "ABC1234", marca = "F", modelo = "X", ano = 2020),
		)
		assertEquals("ABC1234", r.placa)
		verify { veiculos.save(any()) }
	}

	@Test
	fun `criar falha sem cliente`() {
		val cid = UUID.randomUUID()
		every { clientes.findById(cid) } returns Optional.empty()
		assertFailsWith<NotFoundException> {
			service.criar(VeiculoRequest(clienteId = cid, placa = "ABC1234", marca = "F", modelo = "X", ano = 2020))
		}
	}

	@Test
	fun `listar vazio`() {
		every { veiculos.findAll() } returns emptyList()
		assertEquals(0, service.listar().size)
	}
}
