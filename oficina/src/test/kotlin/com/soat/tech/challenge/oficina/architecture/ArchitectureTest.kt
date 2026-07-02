package com.soat.tech.challenge.oficina.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Testes de arquitetura (Fase 1): travam a regra de dependência da Clean Architecture
 * para impedir regressões. A direção das dependências deve sempre apontar para dentro,
 * em direção ao domínio.
 */
@DisplayName("Arquitetura (Clean Architecture)")
class ArchitectureTest {

	private val classes = ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages(BASE)

	@Test
	@DisplayName("respeita a arquitetura em camadas (domínio no núcleo)")
	fun layeredArchitectureIsRespected() {
		layeredArchitecture().consideringOnlyDependenciesInLayers()
			.layer("Domain").definedBy("$BASE.domain..")
			.layer("Application").definedBy("$BASE.application..")
			.layer("Presentation").definedBy("$BASE.presentation..")
			.layer("Infrastructure").definedBy("$BASE.infrastructure..")
			.whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
			.whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
			.whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infrastructure")
			.whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Presentation")
			.check(classes)
	}

	@Test
	@DisplayName("o domínio não depende de nenhuma outra camada nem de frameworks")
	fun domainIsFrameworkFree() {
		noClasses().that().resideInAPackage("$BASE.domain..")
			.should().dependOnClassesThat().resideInAnyPackage(
				"$BASE.application..",
				"$BASE.infrastructure..",
				"$BASE.presentation..",
				"org.springframework..",
				"jakarta.persistence..",
				"jakarta.validation..",
				"com.fasterxml.jackson..",
			)
			.because("o domínio deve ser puro e independente de detalhes técnicos")
			.check(classes)
	}

	@Test
	@DisplayName("a aplicação não depende de infraestrutura, apresentação, JPA ou web")
	fun applicationDoesNotDependOnOuterLayers() {
		noClasses().that().resideInAPackage("$BASE.application..")
			.should().dependOnClassesThat().resideInAnyPackage(
				"$BASE.infrastructure..",
				"$BASE.presentation..",
				"jakarta.persistence..",
				"org.springframework.web..",
				"org.springframework.data..",
			)
			.because("a aplicação orquestra casos de uso através de portas, sem conhecer detalhes técnicos")
			.check(classes)
	}

	@Test
	@DisplayName("a apresentação não depende diretamente da infraestrutura")
	fun presentationDoesNotDependOnInfrastructure() {
		noClasses().that().resideInAPackage("$BASE.presentation..")
			.should().dependOnClassesThat().resideInAPackage("$BASE.infrastructure..")
			.because("controllers devem depender de portas/serviços de aplicação, não de adapters")
			.check(classes)
	}

	@Test
	@DisplayName("ports (Repository/Port) são interfaces e vivem no domínio")
	fun portsAreInterfacesInDomain() {
		classes().that().haveSimpleNameEndingWith("Repository").and().areInterfaces()
			.and().resideOutsideOfPackage("$BASE.infrastructure..")
			.should().resideInAPackage("$BASE.domain.port..")
			.because("ports de repositório do domínio são interfaces em domain.port; repositórios Spring Data ficam na infraestrutura")
			.check(classes)

		classes().that().haveSimpleNameEndingWith("Port").and().areInterfaces()
			.should().resideInAPackage("$BASE.domain.port..")
			.check(classes)
	}

	@Test
	@DisplayName("entidades JPA ficam restritas à infraestrutura")
	fun jpaEntitiesLiveInInfrastructure() {
		classes().that().areAnnotatedWith("jakarta.persistence.Entity")
			.should().resideInAPackage("$BASE.infrastructure.jpa.entity..")
			.because("entidades de persistência não podem vazar para o domínio")
			.check(classes)
	}

	/**
	 * Meta de pureza total (habilitar após a Fase 5): a camada de aplicação deve ficar
	 * livre de qualquer dependência de Spring (@Service/@Transactional movidos para
	 * configuração na infraestrutura).
	 */
	@Test
	@Disabled("Habilitar após a Fase 5 — remover anotações Spring da camada de aplicação")
	@DisplayName("a aplicação é livre de Spring (meta de pureza total)")
	fun applicationIsSpringFree() {
		noClasses().that().resideInAPackage("$BASE.application..")
			.should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
			.check(classes)
	}

	companion object {
		private const val BASE = "com.soat.tech.challenge.oficina"
	}
}
