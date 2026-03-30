package com.soat.tech.challenge.oficina.domain.port

import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import java.util.Optional
import java.util.UUID

interface OrdemServicoRepository {
	fun save(ordem: OrdemServico): OrdemServico
	fun findById(id: UUID): Optional<OrdemServico>
	fun findByTrackingCode(code: String): Optional<OrdemServico>
	fun findAll(): List<OrdemServico>
}
