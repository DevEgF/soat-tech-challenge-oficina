package com.soat.tech.challenge.oficina.domain.model

/**
 * Estados da ordem de serviço (conforme Tech Challenge).
 */
enum class StatusOrdemServico {
	RECEBIDA,
	EM_DIAGNOSTICO,
	AGUARDANDO_APROVACAO,
	EM_EXECUCAO,
	FINALIZADA,
	ENTREGUE,
}
