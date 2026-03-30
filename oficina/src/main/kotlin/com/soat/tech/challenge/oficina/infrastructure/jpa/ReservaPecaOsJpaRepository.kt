package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ReservaPecaOsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ReservaPecaOsJpaRepository : JpaRepository<ReservaPecaOsEntity, String> {

	fun deleteByWorkOrder_IdAndStatus(workOrderId: String, status: String)

	fun findByWorkOrder_IdAndStatus(workOrderId: String, status: String): List<ReservaPecaOsEntity>

	@Query(
		"""
		SELECT COALESCE(SUM(r.quantity), 0)
		FROM ReservaPecaOsEntity r
		WHERE r.part.id = :partId AND r.status = :status
		""",
	)
	fun sumQuantityByPartIdAndStatus(
		@Param("partId") partId: String,
		@Param("status") status: String,
	): Int

	@Query(
		"""
		SELECT COALESCE(SUM(r.quantity), 0)
		FROM ReservaPecaOsEntity r
		WHERE r.part.id = :partId AND r.status = :status AND r.workOrder.id <> :excludeWoId
		""",
	)
	fun sumQuantityByPartIdAndStatusExcludingWorkOrder(
		@Param("partId") partId: String,
		@Param("status") status: String,
		@Param("excludeWoId") excludeWoId: String,
	): Int
}
