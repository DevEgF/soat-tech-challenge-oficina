package com.soat.tech.challenge.oficina.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
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
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
class OrdemServicoFlowIntegrationTest {

	@Autowired
	private lateinit var webApplicationContext: WebApplicationContext

	private val objectMapper = ObjectMapper()

	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setup() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
			.apply<DefaultMockMvcBuilder>(springSecurity())
			.build()
	}

	private fun loginToken(username: String, password: String): String {
		val body = mockMvc
			.perform(
				post("/api/public/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""{"username":"$username","password":"$password"}"""),
			)
			.andExpect(status().isOk)
			.andReturn()
			.response
			.contentAsString
		return objectMapper.readTree(body)["accessToken"].asText()
	}

	private fun postJsonWithScope(url: String, json: String, scope: String, expectedStatus: Int = 200): String =
		mockMvc
			.perform(
				post(url)
					.with(jwt().authorities(SimpleGrantedAuthority(scope)))
					.contentType(MediaType.APPLICATION_JSON)
					.content(json),
			)
			.andExpect(status().`is`(expectedStatus))
			.andReturn()
			.response
			.contentAsString

	@Test
	fun `fluxo swimlane catalogo peca os reservas almoxarife cliente e metricas`() {
		kotlin.test.assertTrue(loginToken("admin", "admin").isNotBlank())

		val servicoJson = postJsonWithScope(
			"/api/admin/servicos-catalogo",
			"""{"nome":"Troca de oleo","descricao":"Exemplo","precoCentavos":15000,"tempoEstimadoMinutos":45}""",
			"SCOPE_ADMIN",
			201,
		)
		val servicoId = objectMapper.readTree(servicoJson)["id"].asText()

		val pecaJson = postJsonWithScope(
			"/api/admin/pecas",
			"""{"codigo":"FILTRO-01","nome":"Filtro oleo","precoCentavos":3500,"quantidadeEstoque":10,"pontoReposicao":5}""",
			"SCOPE_ADMIN",
			201,
		)
		val pecaId = objectMapper.readTree(pecaJson)["id"].asText()

		val osJson = postJsonWithScope(
			"/api/attendant/ordens-servico",
			"""
			{
			  "documentoCliente": "529.982.247-25",
			  "nomeCliente": "Maria Teste",
			  "placa": "ABC1D23",
			  "marca": "VW",
			  "modelo": "Gol",
			  "anoVeiculo": 2020,
			  "servicos": [{"servicoCatalogoId": "$servicoId", "quantidade": 1}],
			  "pecas": [{"pecaId": "$pecaId", "quantidade": 2}]
			}
			""".trimIndent(),
			"SCOPE_ATTENDANT",
			201,
		)
		val os: JsonNode = objectMapper.readTree(osJson)
		val osId = os["id"].asText()
		val codigo = os["codigoAcompanhamento"].asText()

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO_INTERNA"))

		mockMvc.perform(
			post("/api/admin/ordens-servico/$osId/aprovar-interno")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO_INTERNA"))

		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/enviar-orcamento-cliente")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ATTENDANT"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))

		mockMvc.perform(
			post("/api/public/os/aprovar-orcamento")
				.param("documento", "52998224725")
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("EM_EXECUCAO"))

		mockMvc.perform(
			post("/api/warehouse/ordens-servico/$osId/confirmar-saida")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_WAREHOUSE"))),
		).andExpect(status().isNoContent)

		mockMvc.perform(
			get("/api/public/os/acompanhar")
				.param("documento", "52998224725")
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
			.andExpect(jsonPath("$.placaVeiculo").value("ABC1D23"))

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/concluir-servicos")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_TECHNICIAN"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("FINALIZADA"))

		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/registrar-entrega")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ATTENDANT"))),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("ENTREGUE"))

		mockMvc.perform(
			get("/api/admin/metricas/tempo-medio-execucao-servicos")
				.with(jwt().authorities(SimpleGrantedAuthority("SCOPE_ADMIN"))),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[0].servicoCatalogoId").value(UUID.fromString(servicoId).toString()))
	}

	@Test
	fun `admin sem token recebe nao autorizado`() {
		mockMvc.perform(get("/api/admin/clientes")).andExpect(status().isUnauthorized)
	}

	@Test
	fun `jwt do login admin permite listar clientes`() {
		val t = loginToken("admin", "admin")
		mockMvc.perform(
			get("/api/admin/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer $t"),
		).andExpect(status().isOk)
	}

	@Test
	fun `atendente nao acessa endpoint admin de pecas`() {
		val t = loginToken("atendente", "atendente")
		mockMvc.perform(
			get("/api/admin/pecas").header(HttpHeaders.AUTHORIZATION, "Bearer $t"),
		).andExpect(status().isForbidden)
	}
}
