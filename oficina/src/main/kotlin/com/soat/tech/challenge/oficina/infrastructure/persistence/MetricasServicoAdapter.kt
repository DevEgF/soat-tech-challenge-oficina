package com.soat.tech.challenge.oficina.infrastructure.persistence

import com.soat.tech.challenge.oficina.domain.port.MetricasServicoPort
import com.soat.tech.challenge.oficina.domain.port.TempoMedioServicoDto
import com.soat.tech.challenge.oficina.infrastructure.jpa.OrdemServicoJpaRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class MetricasServicoAdapter(
	private val ordemServicoJpa: OrdemServicoJpaRepository,
) : MetricasServicoPort {

	override fun tempoMedioExecucaoPorServico(): List<TempoMedioServicoDto> {
		val ordens = ordemServicoJpa.findAllComDetalhes().filter { o ->
			(o.status == "FINALIZADA" || o.status == "ENTREGUE") &&
				o.execucaoIniciadaEm != null &&
				o.finalizadaEm != null
		}
		data class Acc(var somaMinutos: Double = 0.0, var n: Long = 0)
		val porServico = mutableMapOf<UUID, Acc>()
		val nomes = mutableMapOf<UUID, String>()
		for (o in ordens) {
			val ini = o.execucaoIniciadaEm!!
			val fim = o.finalizadaEm!!
			val minutos = Duration.between(ini, fim).toMinutes().toDouble()
			if (minutos < 0) continue
			for (ls in o.linhasServico.toList()) {
				val sid = UUID.fromString(ls.servicoCatalogo!!.id)
				nomes[sid] = ls.servicoCatalogo!!.nome
				val acc = porServico.getOrPut(sid) { Acc() }
				acc.somaMinutos += minutos
				acc.n += 1
			}
		}
		return porServico.map { (id, acc) ->
			TempoMedioServicoDto(
				servicoCatalogoId = id,
				nomeServico = nomes[id] ?: "",
				mediaMinutos = if (acc.n > 0) acc.somaMinutos / acc.n else 0.0,
				amostras = acc.n,
			)
		}.sortedBy { it.nomeServico }
	}
}
