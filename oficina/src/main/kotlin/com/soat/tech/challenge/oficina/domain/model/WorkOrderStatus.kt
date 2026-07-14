package com.soat.tech.challenge.oficina.domain.model

/**
 * Work order lifecycle states (Tech Challenge). Enum names match persisted API/DB values.
 */
enum class WorkOrderStatus {
    RECEIVED,
    IN_DIAGNOSIS,
    PENDING_INTERNAL_APPROVAL,
    PENDING_APPROVAL,
    AWAITING_PARTS_RELEASE,
    IN_EXECUTION,
    FINALIZED,
    DELIVERED,
    CANCELLED;

    val listingPriority: Int get() = when (this) {
        IN_EXECUTION -> 0
        PENDING_APPROVAL -> 1
        IN_DIAGNOSIS -> 2
        RECEIVED -> 3
        else -> 4 // demais estados visíveis após os prioritários
    }

    val hiddenFromListing: Boolean get() = this == FINALIZED || this == DELIVERED

    /**
     * Portuguese label required at the API surface by the Tech Challenge spec.
     * Internal swimlane states not named in the spec map to the closest customer-facing stage.
     */
    val label: String get() = when (this) {
        RECEIVED -> "Recebida"
        IN_DIAGNOSIS -> "Diagnóstico"
        PENDING_INTERNAL_APPROVAL -> "Diagnóstico"
        PENDING_APPROVAL -> "Aguardando Aprovação"
        AWAITING_PARTS_RELEASE -> "Execução"
        IN_EXECUTION -> "Execução"
        FINALIZED -> "Finalizada"
        DELIVERED -> "Entregue"
        CANCELLED -> "Cancelada"
    }
}
