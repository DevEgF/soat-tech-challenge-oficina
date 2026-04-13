package com.soat.tech.challenge.oficina.presentation

import com.soat.tech.challenge.oficina.domain.exception.BusinessRuleException
import com.soat.tech.challenge.oficina.domain.exception.DomainException
import com.soat.tech.challenge.oficina.domain.exception.InsufficientStockException
import com.soat.tech.challenge.oficina.domain.exception.InvalidLicensePlateException
import com.soat.tech.challenge.oficina.domain.exception.InvalidStatusTransitionException
import com.soat.tech.challenge.oficina.domain.exception.InvalidTaxDocumentException
import com.soat.tech.challenge.oficina.domain.exception.NotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException

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
		HandlerMethodValidationException::class,
	)
	fun badRequest(e: Exception): ResponseEntity<ErrorBody> {
		val msg = when (e) {
			is MethodArgumentNotValidException ->
				e.bindingResult.fieldErrors.joinToString { "${it.field}: ${it.defaultMessage}" }
			is HandlerMethodValidationException ->
				e.allErrors.joinToString { it.defaultMessage ?: "Invalid parameter" }
			else -> e.message ?: "Bad request"
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(msg))
	}

	@ExceptionHandler(
		InvalidStatusTransitionException::class,
		InsufficientStockException::class,
		BusinessRuleException::class,
	)
	fun conflictDomain(e: DomainException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody(e.message ?: "Business rule violation"))

	@ExceptionHandler(IllegalStateException::class)
	fun conflictState(e: IllegalStateException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody(e.message ?: "Invalid state"))

	@ExceptionHandler(DataIntegrityViolationException::class)
	fun dataIntegrityViolation(e: DataIntegrityViolationException) =
		ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorBody("Data conflict: a record with this data already exists"))

	@ExceptionHandler(BadCredentialsException::class)
	fun unauthorized(e: BadCredentialsException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody("Invalid credentials"))

	@ExceptionHandler(AuthenticationException::class)
	fun authenticationFailed(e: AuthenticationException) =
		ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorBody(e.message ?: "Unauthorized"))
}
