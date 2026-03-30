package com.soat.tech.challenge.oficina.application.api

import com.soat.tech.challenge.oficina.application.api.dto.ClienteResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaPecaResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoLinhaServicoResponse
import com.soat.tech.challenge.oficina.application.api.dto.OrdemServicoResponse
import com.soat.tech.challenge.oficina.application.api.dto.PecaResponse
import com.soat.tech.challenge.oficina.application.api.dto.ServicoCatalogoResponse
import com.soat.tech.challenge.oficina.application.api.dto.VeiculoResponse
import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import java.util.UUID

fun Cliente.toResponse(): ClienteResponse = ClienteResponse(
	id = id,
	taxIdDigits = fiscalDocument.digits,
	name = name,
	email = email,
	phone = phone,
)

fun Veiculo.toResponse(): VeiculoResponse = VeiculoResponse(
	id = id,
	customerId = customerId,
	plate = licensePlate.normalized,
	brand = brand,
	model = model,
	year = year,
)

fun ServicoCatalogo.toResponse(): ServicoCatalogoResponse = ServicoCatalogoResponse(
	id = id,
	name = name,
	description = description,
	priceCents = priceCents,
	estimatedMinutes = estimatedMinutes,
)

fun Peca.toResponse(): PecaResponse = PecaResponse(
	id = id,
	code = code,
	name = name,
	priceCents = priceCents,
	stockQuantity = stockQuantity,
	replenishmentPoint = replenishmentPoint,
)

fun OrdemServico.toResponse(
	serviceName: (UUID) -> String? = { null },
	partName: (UUID) -> String? = { null },
): OrdemServicoResponse = OrdemServicoResponse(
	id = id,
	trackingCode = trackingCode,
	customerId = customerId,
	vehicleId = vehicleId,
	status = status,
	servicesTotalCents = servicesTotalCents,
	partsTotalCents = partsTotalCents,
	totalCents = totalCents,
	services = serviceLines.map {
		OrdemServicoLinhaServicoResponse(
			catalogServiceId = it.catalogServiceId,
			serviceName = serviceName(it.catalogServiceId),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	},
	parts = partLines.map {
		OrdemServicoLinhaPecaResponse(
			partId = it.partId,
			partName = partName(it.partId),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	},
)
