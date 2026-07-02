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
}
