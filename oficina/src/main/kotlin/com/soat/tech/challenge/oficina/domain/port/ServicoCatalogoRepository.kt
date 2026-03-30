package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import java.util.Optional
import java.util.UUID

interface ServicoCatalogoRepository {
	fun save(servico: ServicoCatalogo): ServicoCatalogo
	fun findById(id: UUID): Optional<ServicoCatalogo>
	fun findAll(): List<ServicoCatalogo>
	fun deleteById(id: UUID)
}
