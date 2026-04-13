package com.soat.tech.challenge.oficina.application.api.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.soat.tech.challenge.oficina.domain.model.WorkOrderStatus
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(description = "Corpo de login administrativo")
data class LoginRequest(
	@field:NotBlank val username: String = "",
	@field:NotBlank val password: String = "",
)

data class LoginResponse(val accessToken: String, val tokenType: String = "Bearer", val expiresInSeconds: Long)

data class CustomerRequest(
	@JsonProperty("taxId")
	@field:NotBlank val taxIdDigits: String = "",
	@field:NotBlank val name: String = "",
	val email: String? = null,
	val phone: String? = null,
)

data class CustomerResponse(
	val id: UUID,
	@get:JsonProperty("taxId") val taxIdDigits: String,
	val name: String,
	val email: String?,
	val phone: String?,
)

data class VehicleRequest(
	@field:NotNull val customerId: UUID? = null,
	@field:NotBlank val plate: String = "",
	@field:NotBlank val brand: String = "",
	@field:NotBlank val model: String = "",
	@field:NotNull @field:Min(1900) val year: Int? = null,
)

data class VehicleResponse(
	val id: UUID,
	val customerId: UUID,
	val plate: String,
	val brand: String,
	val model: String,
	val year: Int,
)

data class CatalogServiceRequest(
	@field:NotBlank val name: String = "",
	val description: String? = null,
	@field:NotNull @field:Min(0) val priceCents: Long? = null,
	@field:NotNull @field:Min(1) val estimatedMinutes: Int? = null,
)

data class CatalogServiceResponse(
	val id: UUID,
	val name: String,
	val description: String?,
	val priceCents: Long,
	val estimatedMinutes: Int,
)

data class PartRequest(
	@field:NotBlank val code: String = "",
	@field:NotBlank val name: String = "",
	@field:NotNull @field:Min(0) val priceCents: Long? = null,
	@field:NotNull @field:Min(0) val stockQuantity: Int? = null,
	@field:Min(0) val replenishmentPoint: Int? = null,
)

data class PartResponse(
	val id: UUID,
	val code: String,
	val name: String,
	val priceCents: Long,
	val stockQuantity: Int,
	val replenishmentPoint: Int?,
)

data class GoodsReceiptRequest(
	@field:NotNull @field:Min(1) val quantity: Int? = null,
	val reference: String? = null,
)

data class WorkOrderServiceLineRequest(
	@field:NotNull val catalogServiceId: UUID? = null,
	@field:NotNull @field:Min(1) val quantity: Int? = null,
)

data class WorkOrderPartLineRequest(
	@field:NotNull val partId: UUID? = null,
	@field:NotNull @field:Min(1) val quantity: Int? = null,
)

data class CreateWorkOrderRequest(
	@JsonProperty("customerTaxId")
	@field:NotBlank val customerTaxIdDigits: String = "",
	@field:NotBlank val customerName: String = "",
	val customerEmail: String? = null,
	val customerPhone: String? = null,
	@field:NotBlank val plate: String = "",
	@field:NotBlank val vehicleBrand: String = "",
	@field:NotBlank val vehicleModel: String = "",
	@field:NotNull @field:Min(1900) val vehicleYear: Int? = null,
	@field:Valid val services: List<WorkOrderServiceLineRequest> = emptyList(),
	@field:Valid val parts: List<WorkOrderPartLineRequest> = emptyList(),
)

data class WorkOrderServiceLineResponse(
	val catalogServiceId: UUID,
	val serviceName: String?,
	val quantity: Int,
	val unitPriceCents: Long,
)

data class WorkOrderPartLineResponse(
	val partId: UUID,
	val partName: String?,
	val quantity: Int,
	val unitPriceCents: Long,
)

data class WorkOrderResponse(
	val id: UUID,
	val trackingCode: String,
	val customerId: UUID,
	val vehicleId: UUID,
	val status: WorkOrderStatus,
	val servicesTotalCents: Long,
	val partsTotalCents: Long,
	val totalCents: Long,
	val services: List<WorkOrderServiceLineResponse>,
	val parts: List<WorkOrderPartLineResponse>,
)

data class WorkOrderTrackingResponse(
	val trackingCode: String,
	val status: WorkOrderStatus,
	val totalCents: Long,
	val vehiclePlate: String,
	val maskedCustomerTaxId: String,
)

data class AverageServiceTimeResponse(
	val catalogServiceId: UUID,
	val serviceName: String,
	val averageMinutes: Double,
	val sampleCount: Long,
)

data class PartReservationResponse(
	val id: UUID,
	val workOrderId: UUID,
	val partId: UUID,
	val partName: String,
	val quantity: Int,
	val status: String,
)

data class LowStockAlertResponse(
	val partId: UUID,
	val code: String,
	val name: String,
	val stockQuantity: Int,
	val replenishmentPoint: Int?,
	val pendingReservedQuantity: Int,
)
