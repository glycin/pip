package com.glycin.pipserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PipServerApplication

fun main(args: Array<String>) {
    runApplication<PipServerApplication>(*args)
}
