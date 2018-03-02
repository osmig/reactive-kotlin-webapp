package com.r2m.kotlin.reactivewebapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveWebappApplication

fun main(args: Array<String>) {
    runApplication<ReactiveWebappApplication>(*args)
}
