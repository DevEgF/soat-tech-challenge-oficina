package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.model.Cliente
import com.soat.tech.challenge.oficina.domain.model.DocumentoFiscal
import com.soat.tech.challenge.oficina.domain.model.LinhaPecaOrdem
import com.soat.tech.challenge.oficina.domain.model.LinhaServicoOrdem
import com.soat.tech.challenge.oficina.domain.model.OrdemServico
import com.soat.tech.challenge.oficina.domain.model.Peca
import com.soat.tech.challenge.oficina.domain.model.PlacaVeiculo
import com.soat.tech.challenge.oficina.domain.model.ServicoCatalogo
import com.soat.tech.challenge.oficina.domain.model.StatusOrdemServico
import com.soat.tech.challenge.oficina.domain.model.Veiculo
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ClienteEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.OrdemServicoEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.PecaEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.ServicoCatalogoEntity
import com.soat.tech.challenge.oficina.infrastructure.jpa.entity.VeiculoEntity
import java.util.UUID

fun ClienteEntity.toDomain(): Cliente = Cliente(
	id = UUID.fromString(id),
	fiscalDocument = DocumentoFiscal.parse(documentDigits),
	name = name,
	email = email,
	phone = phone,
)

fun Cliente.toEntity(): ClienteEntity = ClienteEntity(
	id = id.toString(),
	documentDigits = fiscalDocument.digits,
	name = name,
	email = email,
	phone = phone,
)

fun VeiculoEntity.toDomain(): Veiculo = Veiculo(
	id = UUID.fromString(id),
	customerId = UUID.fromString(customer!!.id),
	licensePlate = PlacaVeiculo.parse(licensePlate),
	brand = brand,
	model = model,
	year = year,
)

fun ServicoCatalogoEntity.toDomain(): ServicoCatalogo = ServicoCatalogo(
	id = UUID.fromString(id),
	name = name,
	description = description,
	priceCents = priceCents,
	estimatedMinutes = estimatedMinutes,
)

fun PecaEntity.toDomain(): Peca = Peca(
	id = UUID.fromString(id),
	code = code,
	name = name,
	priceCents = priceCents,
	stockQuantity = stockQuantity,
	replenishmentPoint = replenishmentPoint,
)

fun OrdemServicoEntity.toDomain(): OrdemServico {
	val linesS = serviceLines.toList().map {
		LinhaServicoOrdem(
			catalogServiceId = UUID.fromString(it.catalogService!!.id),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	}.toMutableList()
	val linesP = partLines.toList().map {
		LinhaPecaOrdem(
			partId = UUID.fromString(it.part!!.id),
			quantity = it.quantity,
			unitPriceCents = it.unitPriceCents,
		)
	}.toMutableList()
	return OrdemServico(
		id = UUID.fromString(id),
		trackingCode = trackingCode,
		customerId = UUID.fromString(customer!!.id),
		vehicleId = UUID.fromString(vehicle!!.id),
		status = StatusOrdemServico.valueOf(status),
		serviceLines = linesS,
		partLines = linesP,
		servicesTotalCents = servicesTotalCents,
		partsTotalCents = partsTotalCents,
		totalCents = totalCents,
		diagnosedAt = diagnosedAt,
		planSubmittedAt = planSubmittedAt,
		internalApprovedAt = internalApprovedAt,
		quoteSentAt = quoteSentAt,
		approvedAt = approvedAt,
		workStartedAt = workStartedAt,
		completedAt = completedAt,
		deliveredAt = deliveredAt,
		cancelledAt = cancelledAt,
	)
}
