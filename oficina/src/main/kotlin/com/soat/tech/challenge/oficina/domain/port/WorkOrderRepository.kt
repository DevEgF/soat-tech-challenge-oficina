package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.WorkOrder
import java.util.Optional
import java.util.UUID

interface WorkOrderRepository {
	fun save(ordem: WorkOrder): WorkOrder
	fun findById(id: UUID): Optional<WorkOrder>
	fun findByTrackingCode(code: String): Optional<WorkOrder>
	fun findAll(): List<WorkOrder>
}
