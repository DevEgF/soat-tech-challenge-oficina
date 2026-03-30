package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PecaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PecaJpaRepository : JpaRepository<PecaEntity, String> {
	fun findByCode(code: String): PecaEntity?

	@Query(
		"""
		SELECT p FROM PecaEntity p
		WHERE p.replenishmentPoint IS NOT NULL AND p.stockQuantity <= p.replenishmentPoint
		""",
	)
	fun findAtOrBelowReplenishment(): List<PecaEntity>
}
