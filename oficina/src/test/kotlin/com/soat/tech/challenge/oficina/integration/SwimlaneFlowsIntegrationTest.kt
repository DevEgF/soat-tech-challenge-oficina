package com.soat.tech.challenge.oficina.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import kotlin.random.Random
import java.util.UUID

/**
 * Um caso de integração por fluxo de negócio (swimlane / máquina de estados).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Fluxos swimlane (integração)")
class SwimlaneFlowsIntegrationTest {

	@Autowired
	private lateinit var webApplicationContext: WebApplicationContext

	private val mapper = ObjectMapper()
	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
	}

	private fun suffix(): String = UUID.randomUUID().toString().substring(0, 8)

	/** Placa válida (padrão antigo ABC9999), única por chamada. */
	private fun uniquePlate(): String = "ABC${Random.nextInt(1000, 10000)}"

	private fun postJson(url: String, json: String, scope: String, expected: Int = 200): String =
		mockMvc.perform(
			post(url)
				.with(jwt().authorities(SimpleGrantedAuthority(scope)))
				.contentType(MediaType.APPLICATION_JSON)
				.content(json),
		)
			.andExpect(status().`is`(expected))
			.andReturn().response.contentAsString

	private fun seedCatalog(stock: Int, pontoReposicao: Int? = 5, pecaCode: String): Pair<String, String> {
		val servicoJson = postJson(
			"/api/admin/servicos-catalogo",
			"""{"nome":"Svc-${pecaCode}","descricao":"x","precoCentavos":1000,"tempoEstimadoMinutos":30}""",
			"SCOPE_ADMIN",
			201,
		)
		val servicoId = mapper.readTree(servicoJson)["id"].asText()
		val pontoJson = if (pontoReposicao != null) """"pontoReposicao":$pontoReposicao""" else ""
		val pecaBody = if (pontoJson.isNotEmpty()) {
			"""{"codigo":"$pecaCode","nome":"Peca","precoCentavos":100,"quantidadeEstoque":$stock,$pontoJson}"""
		} else {
			"""{"codigo":"$pecaCode","nome":"Peca","precoCentavos":100,"quantidadeEstoque":$stock}"""
		}
		val pecaJson = postJson("/api/admin/pecas", pecaBody, "SCOPE_ADMIN", 201)
		val pecaId = mapper.readTree(pecaJson)["id"].asText()
		return servicoId to pecaId
	}

	private fun createOs(
		servicoId: String,
		pecaId: String,
		partQty: Int,
		documento: String = "529.982.247-25",
		placa: String,
	): Pair<String, String> {
		val osJson = postJson(
			"/api/attendant/ordens-servico",
			"""
			{
			  "documentoCliente": "$documento",
			  "nomeCliente": "Cliente Teste",
			  "placa": "$placa",
			  "marca": "VW",
			  "modelo": "Gol",
			  "anoVeiculo": 2020,
			  "servicos": [{"servicoCatalogoId": "$servicoId", "quantidade": 1}],
			  "pecas": [{"pecaId": "$pecaId", "quantidade": $partQty}]
			}
			""".trimIndent(),
			"SCOPE_ATTENDANT",
			201,
		)
		val os: JsonNode = mapper.readTree(osJson)
		return os["id"].asText() to os["codigoAcompanhamento"].asText()
	}

	private fun advanceToAguardandoCliente(servicoId: String, pecaId: String, placa: String): Pair<String, String> {
		val (osId, codigo) = createOs(servicoId, pecaId, 1, placa = placa)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/admin/ordens-servico/$osId/aprovar-interno")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/enviar-orcamento-cliente")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ATTENDANT"))),
		).andExpect(status().isOk)
		return osId to codigo
	}

	@Test
	@DisplayName("Fluxo: administrador reprova plano interno → OS cancelada")
	fun fluxoReprovarInterno() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(10, 5, "PEC-RI-$s")
		val (osId, _) = createOs(servicoId, pecaId, 1, placa = uniquePlate())
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/admin/ordens-servico/$osId/reprovar-interno")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN"))),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("CANCELADA"))
	}

	@Test
	@DisplayName("Fluxo: cliente reprova orçamento → OS cancelada")
	fun fluxoClienteReprovaOrcamento() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(10, 5, "PEC-CR-$s")
		val (osId, codigo) = advanceToAguardandoCliente(servicoId, pecaId, uniquePlate())
		mockMvc.perform(
			post("/api/public/os/reprovar-orcamento")
				.param("documento", "52998224725")
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("CANCELADA"))
		mockMvc.perform(
			get("/api/public/os/acompanhar")
				.param("documento", "52998224725")
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("CANCELADA"))
	}

	@Test
	@DisplayName("Fluxo: atendente volta orçamento ao diagnóstico")
	fun fluxoVoltarDiagnostico() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(10, 5, "PEC-VD-$s")
		val (osId, _) = advanceToAguardandoCliente(servicoId, pecaId, uniquePlate())
		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/voltar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ATTENDANT"))),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
	}

	@Test
	@DisplayName("Fluxo: submeter plano com estoque insuficiente → conflito")
	fun fluxoEstoqueInsuficienteNaReserva() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(2, null, "PEC-EI-$s")
		val (osId, _) = createOs(servicoId, pecaId, 9, placa = uniquePlate())
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isConflict)
	}

	@Test
	@DisplayName("Fluxo: concluir serviços sem confirmação do almoxarife → conflito")
	fun fluxoConcluirSemConfirmarSaida() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(10, 5, "PEC-CS-$s")
		val (osId, codigo) = advanceToAguardandoCliente(servicoId, pecaId, uniquePlate())
		mockMvc.perform(
			post("/api/public/os/aprovar-orcamento")
				.param("documento", "52998224725")
				.param("codigo", codigo),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/concluir-servicos")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isConflict)
	}

	@Test
	@DisplayName("Fluxo: almoxarife lista reservas pendentes após submeter plano")
	fun fluxoAlmoxarifeListaReservasPendentes() {
		val s = suffix()
		val (servicoId, pecaId) = seedCatalog(10, 5, "PEC-LR-$s")
		val (osId, _) = createOs(servicoId, pecaId, 2, placa = uniquePlate())
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk)
		mockMvc.perform(
			get("/api/warehouse/ordens-servico/$osId/reservas-pendentes")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_WAREHOUSE"))),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[0].quantity").value(2))
	}

	@Test
	@DisplayName("Fluxo: administrador registra entrada de mercadoria")
	fun fluxoEntradaMercadoria() {
		val s = suffix()
		val (_, pecaId) = seedCatalog(5, 10, "PEC-EM-$s")
		mockMvc.perform(
			post("/api/admin/pecas/$pecaId/entrada-mercadoria")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"quantidade":7,"referencia":"NF-123"}"""),
		).andExpect(status().isOk)
		mockMvc.perform(
			get("/api/admin/pecas/$pecaId")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN"))),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.quantidadeEstoque").value(12))
	}

	@Test
	@DisplayName("Fluxo: almoxarife consulta alertas de estoque baixo")
	fun fluxoAlertaEstoqueBaixo() {
		val s = suffix()
		val code = "PEC-AL-$s"
		seedCatalog(3, 5, code)
		val body = mockMvc.perform(
			get("/api/warehouse/alertas-estoque-baixo")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_WAREHOUSE"))),
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString
		val alerts = mapper.readTree(body)
		val row = alerts.elements().asSequence().firstOrNull { it.path("code").asText() == code }
		assertNotNull(row, "esperado alerta para codigo=$code em $body")
		assertEquals(3, row!!.path("quantidadeEstoque").asInt())
	}
}
