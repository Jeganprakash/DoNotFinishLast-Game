package com.donotfinishlast.webgame

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WebgameApplication

fun main(args: Array<String>) {
	runApplication<WebgameApplication>(*args)
}
