package com.soat.tech.challenge.oficina

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.soat.tech.challenge.oficina"])
class OficinaApplication

fun main(args: Array<String>) {
	runApplication<OficinaApplication>(*args)
}
