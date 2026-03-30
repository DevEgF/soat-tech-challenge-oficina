package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.port.AverageServiceTimeDto
import com.soat.tech.challenge.oficina.domain.port.MetricasServicoPort
import com.soat.tech.challenge.oficina.infrastructure.jpa.OrdemServicoJpaRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class MetricasServicoAdapter(
	private val ordemServicoJpa: OrdemServicoJpaRepository,
) : MetricasServicoPort {

	override fun averageExecutionTimeByService(): List<AverageServiceTimeDto> {
		val orders = ordemServicoJpa.findAllWithDetails().filter { o ->
			(o.status == "FINALIZADA" || o.status == "ENTREGUE") &&
				o.workStartedAt != null &&
				o.completedAt != null
		}
		data class Acc(var sumMinutes: Double = 0.0, var n: Long = 0)
		val byService = mutableMapOf<UUID, Acc>()
		val names = mutableMapOf<UUID, String>()
		for (o in orders) {
			val start = o.workStartedAt!!
			val end = o.completedAt!!
			val minutes = Duration.between(start, end).toMinutes().toDouble()
			if (minutes < 0) continue
			for (line in o.serviceLines.toList()) {
				val sid = UUID.fromString(line.catalogService!!.id)
				names[sid] = line.catalogService!!.name
				val acc = byService.getOrPut(sid) { Acc() }
				acc.sumMinutes += minutes
				acc.n += 1
			}
		}
		return byService.map { (id, acc) ->
			AverageServiceTimeDto(
				catalogServiceId = id,
				serviceName = names[id] ?: "",
				averageMinutes = if (acc.n > 0) acc.sumMinutes / acc.n else 0.0,
				sampleCount = acc.n,
			)
		}.sortedBy { it.serviceName }
	}
}
