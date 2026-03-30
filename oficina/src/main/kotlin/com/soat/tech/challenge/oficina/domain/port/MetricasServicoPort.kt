package com.soat.tech.challenge.oficina.domain.port

import java.util.UUID

data class TempoMedioServicoDto(
	val servicoCatalogoId: UUID,
	val nomeServico: String,
	val mediaMinutos: Double,
	val amostras: Long,
)

interface MetricasServicoPort {
	fun tempoMedioExecucaoPorServico(): List<TempoMedioServicoDto>
}
