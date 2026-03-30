package com.soat.tech.challenge.oficina.infrastructure.jpa

import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ClienteEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ClienteJpaRepository : JpaRepository<ClienteEntity, String> {
	fun findByDocumentDigits(documentDigits: String): ClienteEntity?
}
