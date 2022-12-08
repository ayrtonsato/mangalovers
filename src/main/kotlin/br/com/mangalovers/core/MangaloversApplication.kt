package br.com.mangalovers.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class MangaloversApplication

fun main(args: Array<String>) {
	runApplication<MangaloversApplication>(*args)
}
