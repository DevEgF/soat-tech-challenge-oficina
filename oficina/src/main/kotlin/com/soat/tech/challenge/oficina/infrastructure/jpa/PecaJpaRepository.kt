package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PecaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PecaJpaRepository : JpaRepository<PecaEntity, String> {
	fun findByCode(code: String): PecaEntity?
}
