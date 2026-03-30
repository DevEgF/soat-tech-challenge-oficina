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
		val customer = clienteJpa.findById(ordem.customerId.toString())
			.orElseThrow { IllegalArgumentException("Customer not found") }
		val vehicle = veiculoJpa.findById(ordem.vehicleId.toString())
			.orElseThrow { IllegalArgumentException("Vehicle not found") }
		val e = jpa.findByIdWithDetails(ordem.id.toString()).orElseGet {
			OrdemServicoEntity(
				id = ordem.id.toString(),
				trackingCode = ordem.trackingCode,
				customer = customer,
				vehicle = vehicle,
				status = ordem.status.name,
				servicesTotalCents = ordem.servicesTotalCents,
				partsTotalCents = ordem.partsTotalCents,
				totalCents = ordem.totalCents,
			)
		}
		e.customer = customer
		e.vehicle = vehicle
		e.status = ordem.status.name
		e.servicesTotalCents = ordem.servicesTotalCents
		e.partsTotalCents = ordem.partsTotalCents
		e.totalCents = ordem.totalCents
		e.diagnosedAt = ordem.diagnosedAt
		e.quoteSentAt = ordem.quoteSentAt
		e.approvedAt = ordem.approvedAt
		e.workStartedAt = ordem.workStartedAt
		e.completedAt = ordem.completedAt
		e.deliveredAt = ordem.deliveredAt
		e.serviceLines.clear()
		e.partLines.clear()
		for (l in ordem.serviceLines) {
			val sc = servicoCatalogoJpa.findById(l.catalogServiceId.toString())
				.orElseThrow { IllegalArgumentException("Catalog service not found") }
			val le = OrdemServicoLinhaServicoEntity(
				id = UUID.randomUUID().toString(),
				workOrder = e,
				catalogService = sc,
				quantity = l.quantity,
				unitPriceCents = l.unitPriceCents,
			)
			e.serviceLines.add(le)
		}
		for (l in ordem.partLines) {
			val p = pecaJpa.findById(l.partId.toString())
				.orElseThrow { IllegalArgumentException("Part not found") }
			val le = OrdemServicoLinhaPecaEntity(
				id = UUID.randomUUID().toString(),
				workOrder = e,
				part = p,
				quantity = l.quantity,
				unitPriceCents = l.unitPriceCents,
			)
			e.partLines.add(le)
		}
		jpa.save(e)
		return jpa.findByIdWithDetails(ordem.id.toString()).get().toDomain()
	}

	override fun findById(id: UUID): Optional<OrdemServico> =
		jpa.findByIdWithDetails(id.toString()).map { it.toDomain() }

	override fun findByTrackingCode(code: String): Optional<OrdemServico> =
		jpa.findByTrackingCodeWithDetails(code).map { it.toDomain() }

	override fun findAll(): List<OrdemServico> =
		jpa.findAllWithDetails().map { it.toDomain() }
}
