package com.soat.tech.challenge.oficina.application

import com.soat.tech.challenge.oficina.application.api.dto.TempoMedioServicoResponse
import com.soat.tech.challenge.oficina.domain.port.MetricasServicoPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MetricasApplicationService(
	private val metricas: MetricasServicoPort,
) {

	@Transactional(readOnly = true)
	fun tempoMedioPorServico(): List<TempoMedioServicoResponse> =
		metricas.tempoMedioExecucaoPorServico().map {
			TempoMedioServicoResponse(
				servicoCatalogoId = it.servicoCatalogoId,
				nomeServico = it.nomeServico,
				mediaMinutos = it.mediaMinutos,
				amostras = it.amostras,
			)
		}
}
