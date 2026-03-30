package com.soat.tech.challenge.oficina.domain.model

/**
 * Work order lifecycle states (Tech Challenge). Enum names match persisted API/DB values.
 */
enum class StatusOrdemServico {
	RECEBIDA,
	EM_DIAGNOSTICO,
	AGUARDANDO_APROVACAO,
	EM_EXECUCAO,
	FINALIZADA,
	ENTREGUE,
}
