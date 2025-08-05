package com.glycin.pipscripts

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PipScriptsApplication

fun main(args: Array<String>) {
    runApplication<PipScriptsApplication>(*args)
}
