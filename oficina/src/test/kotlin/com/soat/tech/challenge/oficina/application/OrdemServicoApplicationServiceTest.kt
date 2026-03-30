package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaPecaRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaServicoRequest
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.exception.EstoqueInsuficienteException
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrdemServicoApplicationServiceTest {

	private val ordens = mockk<OrdemServicoRepository>()
	private val clientes = mockk<ClienteRepository>()
	private val veiculos = mockk<VeiculoRepository>()
	private val servicosCatalogo = mockk<ServicoCatalogoRepository>()
	private val pecas = mockk<PecaRepository>()
	private val fixedInstant = Instant.parse("2026-03-01T12:00:00Z")
	private val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
	private val service = OrdemServicoApplicationService(
		ordens,
		clientes,
		veiculos,
		servicosCatalogo,
		pecas,
		clock,
	)

	@Test
	fun `criar ordem nova`() {
		val doc = DocumentoFiscal.parse("52998224725")
		val sid = UUID.randomUUID()
		val pid = UUID.randomUUID()
		every { clientes.findByDocumento(doc) } returns Optional.empty()
		every { clientes.save(any()) } answers { firstArg() }
		every { veiculos.findByPlaca(PlacaVeiculo.parse("ABC1234")) } returns Optional.empty()
		every { veiculos.save(any()) } answers { firstArg() }
		every { servicosCatalogo.findById(sid) } returns Optional.of(
			ServicoCatalogo(sid, "Oleo", null, 1000, 30),
		)
		every { pecas.findById(pid) } returns Optional.of(Peca(pid, "P1", "F", 500, 10))
		every { ordens.save(any()) } answers { firstArg() }
		val req = CriarOrdemServicoRequest(
			documentoCliente = "52998224725",
			nomeCliente = "Novo",
			placa = "ABC1234",
			marca = "VW",
			modelo = "Gol",
			anoVeiculo = 2020,
			servicos = listOf(OrdemServicoLinhaServicoRequest(sid, 1)),
			pecas = listOf(OrdemServicoLinhaPecaRequest(pid, 1)),
		)
		val r = service.criar(req)
		assertEquals(StatusOrdemServico.RECEBIDA, r.status)
		verify { ordens.save(any()) }
	}

	@Test
	fun `iniciar diagnostico`() {
		val id = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		every { ordens.findById(id) } returns Optional.of(os)
		every { ordens.save(any()) } answers { firstArg() }
		val r = service.iniciarDiagnostico(id)
		assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, r.status)
	}

	@Test
	fun `acompanhar falha documento errado`() {
		val os = OrdemServico.nova(
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		every { ordens.findByCodigoAcompanhamento(os.codigoAcompanhamento) } returns Optional.of(os)
		every { clientes.findById(os.clienteId) } returns Optional.of(
			Cliente(os.clienteId, DocumentoFiscal.parse("52998224725"), "A"),
		)
		assertFailsWith<IllegalArgumentException> {
			service.acompanhar("39053344705", os.codigoAcompanhamento)
		}
	}

	@Test
	fun `enviar orcamento e aprovar com estoque`() {
		val id = UUID.randomUUID()
		val pid = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = mutableListOf(LinhaPecaOrdem(pid, 2, 100)),
		)
		os.iniciarDiagnostico(fixedInstant)
		every { ordens.findById(id) } returns Optional.of(os)
		every { pecas.findById(pid) } returns Optional.of(Peca(pid, "C", "N", 100, 5))
		every { pecas.save(any()) } answers { firstArg() }
		every { ordens.save(any()) } answers { firstArg() }
		service.enviarOrcamento(id)
		service.aprovarOrcamento(id)
		assertEquals(StatusOrdemServico.EM_EXECUCAO, os.status)
		verify(atLeast = 1) { pecas.save(any()) }
	}

	@Test
	fun `aprovar falha sem estoque`() {
		val id = UUID.randomUUID()
		val pid = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = mutableListOf(LinhaPecaOrdem(pid, 9, 100)),
		)
		os.iniciarDiagnostico(fixedInstant)
		os.enviarOrcamento(fixedInstant.plusSeconds(1))
		every { ordens.findById(id) } returns Optional.of(os)
		every { pecas.findById(pid) } returns Optional.of(Peca(pid, "C", "N", 100, 2))
		assertFailsWith<EstoqueInsuficienteException> {
			service.aprovarOrcamento(id)
		}
	}

	@Test
	fun `concluir e entregar`() {
		val id = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		os.iniciarDiagnostico(fixedInstant)
		os.enviarOrcamento(fixedInstant.plusSeconds(1))
		os.aprovarOrcamento(fixedInstant.plusSeconds(2))
		every { ordens.findById(id) } returns Optional.of(os)
		every { ordens.save(any()) } answers { firstArg() }
		service.concluirServicos(id)
		service.registrarEntrega(id)
		assertEquals(StatusOrdemServico.ENTREGUE, os.status)
	}

	@Test
	fun `listar e obter ordem`() {
		val id = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		every { ordens.findAll() } returns listOf(os)
		every { ordens.findById(id) } returns Optional.of(os)
		assertEquals(1, service.listar().size)
		assertEquals(id, service.obter(id).id)
	}

	@Test
	fun `voltar para diagnostico`() {
		val id = UUID.randomUUID()
		val os = OrdemServico.nova(
			id = id,
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		os.iniciarDiagnostico(fixedInstant)
		os.enviarOrcamento(fixedInstant.plusSeconds(1))
		every { ordens.findById(id) } returns Optional.of(os)
		every { ordens.save(any()) } answers { firstArg() }
		service.voltarParaDiagnostico(id)
		assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, os.status)
	}

	@Test
	fun `acompanhar com sucesso`() {
		val os = OrdemServico.nova(
			clienteId = UUID.randomUUID(),
			veiculoId = UUID.randomUUID(),
			linhasServico = emptyList(),
			linhasPeca = emptyList(),
		)
		val doc = DocumentoFiscal.parse("52998224725")
		every { ordens.findByCodigoAcompanhamento(os.codigoAcompanhamento) } returns Optional.of(os)
		every { clientes.findById(os.clienteId) } returns Optional.of(Cliente(os.clienteId, doc, "A"))
		every { veiculos.findById(os.veiculoId) } returns Optional.of(
			Veiculo(os.veiculoId, os.clienteId, PlacaVeiculo.parse("XYZ1234"), "F", "M", 2019),
		)
		val r = service.acompanhar("52998224725", os.codigoAcompanhamento)
		assertEquals(os.codigoAcompanhamento, r.codigoAcompanhamento)
		assertEquals("XYZ1234", r.placaVeiculo)
	}
}
