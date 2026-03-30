package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.AcompanhamentoOsResponse
import com.soat.tech.challenge.oficina.application.api.dto.CriarOrdemServicoRequest
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import com.soat.tech.challenge.oficina.application.api.toResponse
import com.soat.tech.challenge.oficina.domain.exception.EstoqueInsuficienteException
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.LinhaServicoOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.domain.port.ClienteRepository
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.domain.port.PecaRepository
import com.soat.tech.challenge.oficina.domain.port.ServicoCatalogoRepository
import com.soat.tech.challenge.oficina.domain.port.VeiculoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class OrdemServicoApplicationService(
	private val ordens: OrdemServicoRepository,
	private val clientes: ClienteRepository,
	private val veiculos: VeiculoRepository,
	private val servicosCatalogo: ServicoCatalogoRepository,
	private val pecas: PecaRepository,
	private val clock: Clock,
) {

	private fun catalogoNome(id: UUID): String? =
		servicosCatalogo.findById(id).map { it.nome }.orElse(null)

	private fun pecaNome(id: UUID): String? =
		pecas.findById(id).map { it.nome }.orElse(null)

	private fun OrdemServico.toDto(): OrdemServicoResponse =
		toResponse({ catalogoNome(it) }, { pecaNome(it) })

	@Transactional
	fun criar(req: CriarOrdemServicoRequest): OrdemServicoResponse {
		val doc = DocumentoFiscal.parse(req.documentoCliente)
		val cliente = clientes.findByDocumento(doc).orElseGet {
			clientes.save(
				Cliente(
					id = UUID.randomUUID(),
					documento = doc,
					nome = req.nomeCliente.trim(),
					email = req.emailCliente?.trim()?.takeIf { it.isNotEmpty() },
					telefone = req.telefoneCliente?.trim()?.takeIf { it.isNotEmpty() },
				),
			)
		}
		val placa = PlacaVeiculo.parse(req.placa)
		val veiculo = veiculos.findByPlaca(placa).orElseGet {
			veiculos.save(
				Veiculo(
					id = UUID.randomUUID(),
					clienteId = cliente.id,
					placa = placa,
					marca = req.marca.trim(),
					modelo = req.modelo.trim(),
					ano = req.anoVeiculo!!,
				),
			)
		}
		if (veiculo.clienteId != cliente.id) {
			throw IllegalArgumentException("Veículo pertence a outro cliente")
		}
		val linhasServico = req.servicos.map { s ->
			val cat = servicosCatalogo.findById(s.servicoCatalogoId!!)
				.orElseThrow { NotFoundException("Serviço de catálogo não encontrado") }
			LinhaServicoOrdem(
				servicoCatalogoId = cat.id,
				quantidade = s.quantidade!!,
				precoUnitarioCentavos = cat.precoCentavos,
			)
		}
		val linhasPeca = req.pecas.map { p ->
			val peca = pecas.findById(p.pecaId!!)
				.orElseThrow { NotFoundException("Peça não encontrada") }
			LinhaPecaOrdem(
				pecaId = peca.id,
				quantidade = p.quantidade!!,
				precoUnitarioCentavos = peca.precoCentavos,
			)
		}
		val os = OrdemServico.nova(
			clienteId = cliente.id,
			veiculoId = veiculo.id,
			linhasServico = linhasServico,
			linhasPeca = linhasPeca,
		)
		return ordens.save(os).toDto()
	}

	@Transactional(readOnly = true)
	fun listar(): List<OrdemServicoResponse> = ordens.findAll().map { it.toDto() }

	@Transactional(readOnly = true)
	fun obter(id: UUID): OrdemServicoResponse =
		ordens.findById(id).map { it.toDto() }.orElseThrow { NotFoundException("Ordem de serviço não encontrada") }

	@Transactional(readOnly = true)
	fun acompanhar(documentoCliente: String, codigoAcompanhamento: String): AcompanhamentoOsResponse {
		val doc = DocumentoFiscal.parse(documentoCliente)
		val os = ordens.findByCodigoAcompanhamento(codigoAcompanhamento.trim())
			.orElseThrow { NotFoundException("Ordem não encontrada") }
		val cliente = clientes.findById(os.clienteId).orElseThrow { NotFoundException("Cliente não encontrado") }
		if (cliente.documento.digitos != doc.digitos) {
			throw IllegalArgumentException("Documento não confere com a ordem")
		}
		val veiculo = veiculos.findById(os.veiculoId).orElseThrow { NotFoundException("Veículo não encontrado") }
		return AcompanhamentoOsResponse(
			codigoAcompanhamento = os.codigoAcompanhamento,
			status = os.status,
			valorTotalCentavos = os.valorTotalCentavos,
			placaVeiculo = veiculo.placa.normalizada,
			documentoClienteMascarado = mascararDocumento(doc.digitos),
		)
	}

	private fun mascararDocumento(d: String): String = when (d.length) {
		11 -> "***.${d.take(3)}.${d.substring(3, 6)}-**"
		14 -> "**.${d.substring(2, 5)}.${d.substring(5, 8)}/****-**"
		else -> "***"
	}

	private fun agora() = clock.instant()

	@Transactional
	fun iniciarDiagnostico(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		os.iniciarDiagnostico(agora())
		return ordens.save(os).toDto()
	}

	@Transactional
	fun enviarOrcamento(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		os.enviarOrcamento(agora())
		return ordens.save(os).toDto()
	}

	@Transactional
	fun aprovarOrcamento(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		baixarEstoque(os)
		os.aprovarOrcamento(agora())
		return ordens.save(os).toDto()
	}

	private fun baixarEstoque(os: OrdemServico) {
		for (linha in os.linhasPeca) {
			val peca = pecas.findById(linha.pecaId).orElseThrow { NotFoundException("Peça não encontrada") }
			if (peca.quantidadeEstoque < linha.quantidade) {
				throw EstoqueInsuficienteException(linha.pecaId.toString(), linha.quantidade, peca.quantidadeEstoque)
			}
			pecas.save(peca.comEstoqueAjustado(-linha.quantidade))
		}
	}

	@Transactional
	fun voltarParaDiagnostico(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		os.voltarParaDiagnostico(agora())
		return ordens.save(os).toDto()
	}

	@Transactional
	fun concluirServicos(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		os.concluirServicos(agora())
		return ordens.save(os).toDto()
	}

	@Transactional
	fun registrarEntrega(id: UUID): OrdemServicoResponse {
		val os = ordens.findById(id).orElseThrow { NotFoundException("Ordem não encontrada") }
		os.registrarEntrega(agora())
		return ordens.save(os).toDto()
	}
}
