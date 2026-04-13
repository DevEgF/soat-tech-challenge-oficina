package com.soat.tech.challenge.oficina.infrastructure.jpa.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "ordens_servico")
class WorkOrderEntity(
	@Id
	@Column(length = 36)
	var id: String = "",
	@Column(nullable = false, unique = true, name = "codigo_acompanhamento", length = 64)
	var trackingCode: String = "",
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cliente_id", nullable = false)
	var customer: CustomerEntity? = null,
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "veiculo_id", nullable = false)
	var vehicle: VehicleEntity? = null,
	@Column(nullable = false, length = 40)
	var status: String = "",
	@Column(nullable = false, name = "valor_servicos_centavos")
	var servicesTotalCents: Long = 0,
	@Column(nullable = false, name = "valor_pecas_centavos")
	var partsTotalCents: Long = 0,
	@Column(nullable = false, name = "valor_total_centavos")
	var totalCents: Long = 0,
	@Column(name = "diagnosticado_em")
	var diagnosedAt: Instant? = null,
	@Column(name = "plano_submetido_em")
	var planSubmittedAt: Instant? = null,
	@Column(name = "aprovacao_interna_em")
	var internalApprovedAt: Instant? = null,
	@Column(name = "orcamento_enviado_em")
	var quoteSentAt: Instant? = null,
	@Column(name = "aprovado_em")
	var approvedAt: Instant? = null,
	@Column(name = "execucao_iniciada_em")
	var workStartedAt: Instant? = null,
	@Column(name = "finalizada_em")
	var completedAt: Instant? = null,
	@Column(name = "entregue_em")
	var deliveredAt: Instant? = null,
	@Column(name = "cancelada_em")
	var cancelledAt: Instant? = null,
	@Column(name = "retornado_diagnostico_em")
	var returnedToDiagnosisAt: Instant? = null,
	@OneToMany(mappedBy = "workOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
	var serviceLines: MutableSet<WorkOrderServiceLineEntity> = mutableSetOf(),
	@OneToMany(mappedBy = "workOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
	var partLines: MutableSet<WorkOrderPartLineEntity> = mutableSetOf(),
)
