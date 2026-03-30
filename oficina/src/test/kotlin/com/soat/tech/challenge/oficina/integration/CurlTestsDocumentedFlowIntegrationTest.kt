package com.soat.tech.challenge.oficina.integration

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
import kotlin.random.Random
import kotlin.test.assertTrue
import java.util.UUID

/**
 * Fluxo ponta a ponta alinhado ao roteiro [CURL_TESTS.md] na raiz do repositório,
 * usando os contratos reais da API (nomes de campos, query params públicos, paths).
 *
 * Diferenças em relação ao markdown legado: login retorna `accessToken`; criação de OS
 * usa `documentoCliente` / `placa` / linhas de serviço e peça; `submeter-plano` e aprovações
 * internas são POST sem corpo; orçamento público usa `documento` e `codigo` como query params;
 * confirmação de saída é `POST /api/warehouse/ordens-servico/{id}/confirmar-saida`;
 * alertas de estoque: `GET /api/warehouse/alertas-estoque-baixo`.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Fluxo CURL_TESTS.md (API real + Bearer)")
class CurlTestsDocumentedFlowIntegrationTest {

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

	private fun uniquePlate(): String = "ABC${Random.nextInt(1000, 10000)}"

	private fun loginToken(username: String, password: String): String {
		val body = mockMvc.perform(
			post("/api/public/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"username":"$username","password":"$password"}"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.accessToken").exists())
			.andReturn().response.contentAsString
		return mapper.readTree(body)["accessToken"].asText()
	}

	private fun postBearer(url: String, token: String, json: String, expected: Int = 200): String =
		mockMvc.perform(
			post(url)
				.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(json),
		)
			.andExpect(status().`is`(expected))
			.andReturn().response.contentAsString

	private fun getBearer(url: String, token: String): String =
		mockMvc.perform(
			get(url).header(HttpHeaders.AUTHORIZATION, "Bearer $token"),
		)
			.andExpect(status().isOk)
			.andReturn().response.contentAsString

	@Test
	@DisplayName("Seções 1–5 + health: logins, cadastro admin, OS swimlane até ENTREGUE, consultas")
	fun fluxoCompletoConformeCurlTestsComBearer() {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("UP"))

		val adminToken = loginToken("admin", "admin")
		val attendantToken = loginToken("atendente", "atendente")
		val techToken = loginToken("tecnico", "tecnico")
		val warehouseToken = loginToken("almoxarife", "almoxarife")
		assertTrue(adminToken.isNotBlank() && attendantToken.isNotBlank())
		assertTrue(techToken.isNotBlank() && warehouseToken.isNotBlank())

		val s = suffix()
		val docDigits = "11222333000181"
		val docFormatted = "11.222.333/0001-81"
		val plate = uniquePlate()

		val clienteJson = postBearer(
			"/api/admin/clientes",
			adminToken,
			"""
			{
			  "nome": "Empresa Curl Test $s",
			  "documento": "$docFormatted",
			  "email": "curl-$s@example.com",
			  "telefone": "11987654321"
			}
			""".trimIndent(),
			201,
		)
		val clienteId = mapper.readTree(clienteJson)["id"].asText()

		postBearer(
			"/api/admin/veiculos",
			adminToken,
			"""
			{
			  "clienteId": "$clienteId",
			  "placa": "$plate",
			  "marca": "Honda",
			  "modelo": "Civic",
			  "ano": 2020
			}
			""".trimIndent(),
			201,
		)

		val servicoJson = postBearer(
			"/api/admin/servicos-catalogo",
			adminToken,
			"""
			{
			  "nome": "Troca de óleo $s",
			  "descricao": "Troca de óleo e filtro",
			  "precoCentavos": 15000,
			  "tempoEstimadoMinutos": 30
			}
			""".trimIndent(),
			201,
		)
		val servicoId = mapper.readTree(servicoJson)["id"].asText()

		val pecaCode = "CURL-FLOW-$s"
		val pecaJson = postBearer(
			"/api/admin/pecas",
			adminToken,
			"""
			{
			  "codigo": "$pecaCode",
			  "nome": "Filtro de óleo",
			  "precoCentavos": 4500,
			  "quantidadeEstoque": 50,
			  "pontoReposicao": 10
			}
			""".trimIndent(),
			201,
		)
		val pecaId = mapper.readTree(pecaJson)["id"].asText()

		postBearer(
			"/api/admin/pecas/$pecaId/entrada-mercadoria",
			adminToken,
			"""{"quantidade":20,"referencia":"NF-CURL-$s"}""",
		)

		mockMvc.perform(
			get("/api/admin/pecas/$pecaId")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.quantidadeEstoque").value(70))

		val osJson = postBearer(
			"/api/attendant/ordens-servico",
			attendantToken,
			"""
			{
			  "documentoCliente": "$docFormatted",
			  "nomeCliente": "Cliente Curl",
			  "placa": "$plate",
			  "marca": "Honda",
			  "modelo": "Civic",
			  "anoVeiculo": 2020,
			  "servicos": [{"servicoCatalogoId": "$servicoId", "quantidade": 1}],
			  "pecas": [{"pecaId": "$pecaId", "quantidade": 2}]
			}
			""".trimIndent(),
			201,
		)
		val os: JsonNode = mapper.readTree(osJson)
		val osId = os["id"].asText()
		val codigo = os["codigoAcompanhamento"].asText()

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/iniciar-diagnostico")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $techToken"),
		).andExpect(status().isOk)

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/submeter-plano")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $techToken"),
		).andExpect(status().isOk)

		mockMvc.perform(
			post("/api/admin/ordens-servico/$osId/aprovar-interno")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken"),
		).andExpect(status().isOk)

		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/enviar-orcamento-cliente")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $attendantToken"),
		).andExpect(status().isOk)

		mockMvc.perform(
			post("/api/public/os/aprovar-orcamento")
				.param("documento", docDigits)
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("EM_EXECUCAO"))

		val reservasJson = getBearer(
			"/api/warehouse/ordens-servico/$osId/reservas-pendentes",
			warehouseToken,
		)
		val reservas = mapper.readTree(reservasJson)
		assertTrue(reservas.isArray && reservas.size() >= 1)
		val temReservaFiltro = reservas.elements().asSequence().any { node ->
			node.path("pecaId").asText() == pecaId && node.path("quantity").asInt() == 2
		}
		assertTrue(temReservaFiltro)

		mockMvc.perform(
			post("/api/warehouse/ordens-servico/$osId/confirmar-saida")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $warehouseToken"),
		).andExpect(status().isNoContent)

		mockMvc.perform(
			get("/api/public/os/acompanhar")
				.param("documento", docDigits)
				.param("codigo", codigo),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
			.andExpect(jsonPath("$.placaVeiculo").value(plate))

		mockMvc.perform(
			post("/api/technician/ordens-servico/$osId/concluir-servicos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $techToken"),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("FINALIZADA"))

		mockMvc.perform(
			post("/api/attendant/ordens-servico/$osId/registrar-entrega")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $attendantToken"),
		).andExpect(status().isOk).andExpect(jsonPath("$.status").value("ENTREGUE"))

		getBearer("/api/warehouse/alertas-estoque-baixo", warehouseToken)

		mockMvc.perform(
			get("/api/admin/metricas/tempo-medio-execucao-servicos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer $adminToken"),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$[0].servicoCatalogoId").value(servicoId))
	}
}
