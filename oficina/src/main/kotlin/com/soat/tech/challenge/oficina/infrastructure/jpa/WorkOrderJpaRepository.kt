package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.WorkOrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional

interface WorkOrderJpaRepository : JpaRepository<WorkOrderEntity, String> {
	@Query(
		"""
		SELECT DISTINCT o FROM WorkOrderEntity o
		LEFT JOIN FETCH o.customer
		LEFT JOIN FETCH o.vehicle
		LEFT JOIN FETCH o.serviceLines sl
		LEFT JOIN FETCH sl.catalogService
		LEFT JOIN FETCH o.partLines pl
		LEFT JOIN FETCH pl.part
		WHERE o.id = :id
		""",
	)
	fun findByIdWithDetails(id: String): Optional<WorkOrderEntity>

	@Query(
		"""
		SELECT DISTINCT o FROM WorkOrderEntity o
		LEFT JOIN FETCH o.customer
		LEFT JOIN FETCH o.vehicle
		LEFT JOIN FETCH o.serviceLines sl
		LEFT JOIN FETCH sl.catalogService
		LEFT JOIN FETCH o.partLines pl
		LEFT JOIN FETCH pl.part
		WHERE o.trackingCode = :code
		""",
	)
	fun findByTrackingCodeWithDetails(code: String): Optional<WorkOrderEntity>

	@Query(
		"""
		SELECT DISTINCT o FROM WorkOrderEntity o
		LEFT JOIN FETCH o.customer
		LEFT JOIN FETCH o.vehicle
		LEFT JOIN FETCH o.serviceLines sl
		LEFT JOIN FETCH sl.catalogService
		LEFT JOIN FETCH o.partLines pl
		LEFT JOIN FETCH pl.part
		""",
	)
	fun findAllWithDetails(): List<WorkOrderEntity>
}
