package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.port.OrdemServicoRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ClienteJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.OrdemServicoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PecaJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.ServicoCatalogoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.VeiculoJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoLinhaPecaEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoLinhaServicoEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class OrdemServicoRepositoryAdapter(
	private val jpa: OrdemServicoJpaRepository,
	private val clienteJpa: ClienteJpaRepository,
	private val veiculoJpa: VeiculoJpaRepository,
	private val servicoCatalogoJpa: ServicoCatalogoJpaRepository,
	private val pecaJpa: PecaJpaRepository,
) : OrdemServicoRepository {

	override fun save(ordem: OrdemServico): OrdemServico {
		val cliente = clienteJpa.findById(ordem.clienteId.toString())
			.orElseThrow { IllegalArgumentException("Cliente não encontrado") }
		val veiculo = veiculoJpa.findById(ordem.veiculoId.toString())
			.orElseThrow { IllegalArgumentException("Veículo não encontrado") }
		val e = jpa.findByIdComDetalhes(ordem.id.toString()).orElseGet {
			OrdemServicoEntity(
				id = ordem.id.toString(),
				codigoAcompanhamento = ordem.codigoAcompanhamento,
				cliente = cliente,
				veiculo = veiculo,
				status = ordem.status.name,
				valorServicosCentavos = ordem.valorServicosCentavos,
				valorPecasCentavos = ordem.valorPecasCentavos,
				valorTotalCentavos = ordem.valorTotalCentavos,
			)
		}
		e.cliente = cliente
		e.veiculo = veiculo
		e.status = ordem.status.name
		e.valorServicosCentavos = ordem.valorServicosCentavos
		e.valorPecasCentavos = ordem.valorPecasCentavos
		e.valorTotalCentavos = ordem.valorTotalCentavos
		e.diagnosticadoEm = ordem.diagnosticadoEm
		e.orcamentoEnviadoEm = ordem.orcamentoEnviadoEm
		e.aprovadoEm = ordem.aprovadoEm
		e.execucaoIniciadaEm = ordem.execucaoIniciadaEm
		e.finalizadaEm = ordem.finalizadaEm
		e.entregueEm = ordem.entregueEm
		e.linhasServico.clear()
		e.linhasPeca.clear()
		for (l in ordem.linhasServico) {
			val sc = servicoCatalogoJpa.findById(l.servicoCatalogoId.toString())
				.orElseThrow { IllegalArgumentException("Serviço catálogo não encontrado") }
			val le = OrdemServicoLinhaServicoEntity(
				id = UUID.randomUUID().toString(),
				ordemServico = e,
				servicoCatalogo = sc,
				quantidade = l.quantidade,
				precoUnitarioCentavos = l.precoUnitarioCentavos,
			)
			e.linhasServico.add(le)
		}
		for (l in ordem.linhasPeca) {
			val p = pecaJpa.findById(l.pecaId.toString())
				.orElseThrow { IllegalArgumentException("Peça não encontrada") }
			val le = OrdemServicoLinhaPecaEntity(
				id = UUID.randomUUID().toString(),
				ordemServico = e,
				peca = p,
				quantidade = l.quantidade,
				precoUnitarioCentavos = l.precoUnitarioCentavos,
			)
			e.linhasPeca.add(le)
		}
		jpa.save(e)
		return jpa.findByIdComDetalhes(ordem.id.toString()).get().toDomain()
	}

	override fun findById(id: UUID): Optional<OrdemServico> =
		jpa.findByIdComDetalhes(id.toString()).map { it.toDomain() }

	override fun findByCodigoAcompanhamento(codigo: String): Optional<OrdemServico> =
		jpa.findByCodigoAcompanhamentoComDetalhes(codigo).map { it.toDomain() }

	override fun findAll(): List<OrdemServico> =
		jpa.findAllComDetalhes().map { it.toDomain() }
}
