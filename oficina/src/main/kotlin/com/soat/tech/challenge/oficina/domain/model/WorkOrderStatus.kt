package com.soat.tech.challenge.oficina.domain.model

/**
 * Work order lifecycle states (Tech Challenge). Enum names match persisted API/DB values.
 */
enum class WorkOrderStatus {
	RECEIVED,
	IN_DIAGNOSIS,
	PENDING_INTERNAL_APPROVAL,
	PENDING_APPROVAL,
	IN_EXECUTION,
	FINALIZED,
	DELIVERED,
	CANCELLED,
}
