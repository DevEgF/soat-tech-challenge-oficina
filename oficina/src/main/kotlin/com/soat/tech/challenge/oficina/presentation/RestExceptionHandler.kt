package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.NotFoundException
import com.soat.tech.challenge.oficina.domain.exception.DocumentoInvalidoException
import com.soat.tech.challenge.oficina.domain.exception.DomainException
import com.soat.tech.challenge.oficina.domain.exception.EstoqueInsuficienteException
import com.soat.tech.challenge.oficina.domain.exception.PlacaInvalidaException
import com.soat.tech.challenge.oficina.domain.exception.TransicaoStatusInvalidaException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorBody(val message: String)

@RestControllerAdvice
class RestExceptionHandler {

	@ExceptionHandler(NotFoundException::class)
	fun notFound(e: NotFoundException) =
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorBody(e.message ?: "Não encontrado"))

	@ExceptionHandler(
		IllegalArgumentException::class,
		DocumentoInvalidoException::class,
		PlacaInvalidaException::class,
		MethodArgumentNotValidException::class,
	)
	fun badRequest(e: Exception): ResponseEntity<ErrorBody> {
		val msg = when (e) {
			is MethodArgumentNotValidException ->
				e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
			else -> e.message ?: "Requisição inválida"
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(msg))
	}

	@ExceptionHandler(
		TransicaoStatusInvalidaException::class,
		EstoqueInsuficienteException::class,
	)
	fun conflictDomain(e: DomainException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody(e.message ?: "Regra de negócio"))

	@ExceptionHandler(BadCredentialsException::class)
	fun unauthorized(e: BadCredentialsException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody("Credenciais inválidas"))

	@ExceptionHandler(AuthenticationException::class)
	fun authenticationFailed(e: AuthenticationException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody(e.message ?: "Não autorizado"))
}
