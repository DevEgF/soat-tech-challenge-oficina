package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.Peca
import java.util.Optional
import java.util.UUID

interface PecaRepository {
	fun save(peca: Peca): Peca
	fun findById(id: UUID): Optional<Peca>
	fun findByCodigo(codigo: String): Optional<Peca>
	fun findAll(): List<Peca>
	fun deleteById(id: UUID)
}
