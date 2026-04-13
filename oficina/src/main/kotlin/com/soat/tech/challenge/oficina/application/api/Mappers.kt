package com.soat.tech.challenge.oficina.application.api

import com.soat.tech.challenge.oficina.application.api.dto.CustomerResponse
import com.soat.tech.challenge.oficina.application.api.dto.WorkOrderPartLineResponse
import com.soat.tech.challenge.oficina.application.api.dto.WorkOrderServiceLineResponse
import com.soat.tech.challenge.oficina.application.api.dto.WorkOrderResponse
import com.soat.tech.challenge.oficina.application.api.dto.PartResponse
import com.soat.tech.challenge.oficina.application.api.dto.CatalogServiceResponse
import com.soat.tech.challenge.oficina.application.api.dto.VehicleResponse
import com.soat.tech.challenge.oficina.domain.model.Customer
import com.soat.tech.challenge.oficina.domain.model.WorkOrder
import com.soat.tech.challenge.oficina.domain.model.Part
import com.soat.tech.challenge.oficina.domain.model.CatalogService
import com.soat.tech.challenge.oficina.domain.model.Vehicle
import java.util.UUID

fun Customer.toResponse(): CustomerResponse = CustomerResponse(
	id = id,
	taxIdDigits = fiscalDocument.digits,
	name = name,
	email = email,
	phone = phone,
)

fun Vehicle.toResponse(): VehicleResponse = VehicleResponse(
	id = id,
	customerId = customerId,
	plate = licensePlate.normalized,
	brand = brand,
	model = model,
	year = year,
)

fun CatalogService.toResponse(): CatalogServiceResponse = CatalogServiceResponse(
	id = id,
	name = name,
	description = description,
	priceCents = priceCents,
	estimatedMinutes = estimatedMinutes,
)

fun Part.toResponse(): PartResponse = PartResponse(
	id = id,
	code = code,
	name = name,
	priceCents = priceCents,
	stockQuantity = stockQuantity,
	replenishmentPoint = replenishmentPoint,
)

fun WorkOrder.toResponse(
	serviceName: (UUID) -> String? = { null },
	partName: (UUID) -> String? = { null },
): WorkOrderResponse = WorkOrderResponse(
	id = id,
	trackingCode = trackingCode,
	customerId = customerId,
	vehicleId = vehicleId,
	status = status,
	servicesTotalCents = servicesTotalCents,
	partsTotalCents = partsTotalCents,
	totalCents = totalCents,
	services = serviceLines.map {
		WorkOrderServiceLineResponse(
			catalogServiceId = it.catalogServiceId,
			serviceName = serviceName(it.catalogServiceId),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	},
	parts = partLines.map {
		WorkOrderPartLineResponse(
			partId = it.partId,
			partName = partName(it.partId),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	},
)
