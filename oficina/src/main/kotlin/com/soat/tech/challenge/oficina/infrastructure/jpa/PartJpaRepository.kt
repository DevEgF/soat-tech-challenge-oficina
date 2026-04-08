package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PartEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PartJpaRepository : JpaRepository<PartEntity, String> {
	fun findByCode(code: String): PartEntity?

	@Query(
		"""
		SELECT p FROM PartEntity p
		WHERE p.replenishmentPoint IS NOT NULL AND p.stockQuantity <= p.replenishmentPoint
		""",
	)
	fun findAtOrBelowReplenishment(): List<PartEntity>
}
