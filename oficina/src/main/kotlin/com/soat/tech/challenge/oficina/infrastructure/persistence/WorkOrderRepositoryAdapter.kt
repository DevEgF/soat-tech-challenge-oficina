package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.WorkOrder
import com.soat.tech.challenge.oficina.domain.port.WorkOrderRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.CustomerJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.WorkOrderJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.PartJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.CatalogServiceJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.VehicleJpaRepository
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.WorkOrderEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.WorkOrderPartLineEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.WorkOrderServiceLineEntity
import org.springframework.stereotype.Component
import java.util.Optional
import java.util.UUID

@Component
class WorkOrderRepositoryAdapter(
	private val jpa: WorkOrderJpaRepository,
	private val clienteJpa: CustomerJpaRepository,
	private val veiculoJpa: VehicleJpaRepository,
	private val servicoCatalogoJpa: CatalogServiceJpaRepository,
	private val pecaJpa: PartJpaRepository,
) : WorkOrderRepository {

	override fun save(ordem: WorkOrder): WorkOrder {
		val customer = clienteJpa.findById(ordem.customerId.toString())
			.orElseThrow { IllegalArgumentException("Customer not found") }
		val vehicle = veiculoJpa.findById(ordem.vehicleId.toString())
			.orElseThrow { IllegalArgumentException("Vehicle not found") }
		val e = jpa.findByIdWithDetails(ordem.id.toString()).orElseGet {
			WorkOrderEntity(
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
		e.planSubmittedAt = ordem.planSubmittedAt
		e.internalApprovedAt = ordem.internalApprovedAt
		e.quoteSentAt = ordem.quoteSentAt
		e.approvedAt = ordem.approvedAt
		e.workStartedAt = ordem.workStartedAt
		e.completedAt = ordem.completedAt
		e.deliveredAt = ordem.deliveredAt
		e.cancelledAt = ordem.cancelledAt
		e.serviceLines.clear()
		e.partLines.clear()
		for (l in ordem.serviceLines) {
			val sc = servicoCatalogoJpa.findById(l.catalogServiceId.toString())
				.orElseThrow { IllegalArgumentException("Catalog service not found") }
			val le = WorkOrderServiceLineEntity(
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
			val le = WorkOrderPartLineEntity(
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

	override fun findById(id: UUID): Optional<WorkOrder> =
		jpa.findByIdWithDetails(id.toString()).map { it.toDomain() }

	override fun findByTrackingCode(code: String): Optional<WorkOrder> =
		jpa.findByTrackingCodeWithDetails(code).map { it.toDomain() }

	override fun findAll(): List<WorkOrder> =
		jpa.findAllWithDetails().map { it.toDomain() }
}
