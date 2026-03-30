package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.application.NotFoundException
import com.soat.tech.challenge.oficina.domain.exception.DomainException
import com.soat.tech.challenge.oficina.domain.exception.InvalidLicensePlateException
import com.soat.tech.challenge.oficina.domain.exception.InvalidTaxDocumentException
import com.soat.tech.challenge.oficina.domain.exception.InsufficientStockException
import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
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
		ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorBody(e.message ?: "Not found"))

	@ExceptionHandler(
		IllegalArgumentException::class,
		InvalidTaxDocumentException::class,
		InvalidLicensePlateException::class,
		MethodArgumentNotValidException::class,
	)
	fun badRequest(e: Exception): ResponseEntity<ErrorBody> {
		val msg = when (e) {
			is MethodArgumentNotValidException ->
				e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
			else -> e.message ?: "Bad request"
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(msg))
	}

	@ExceptionHandler(
		InvalidStatusTransitionException::class,
		InsufficientStockException::class,
	)
	fun conflictDomain(e: DomainException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody(e.message ?: "Business rule violation"))

	@ExceptionHandler(IllegalStateException::class)
	fun conflictState(e: IllegalStateException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody(e.message ?: "Invalid state"))

	@ExceptionHandler(BadCredentialsException::class)
	fun unauthorized(e: BadCredentialsException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody("Invalid credentials"))

	@ExceptionHandler(AuthenticationException::class)
	fun authenticationFailed(e: AuthenticationException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody(e.message ?: "Unauthorized"))
}
