package com.example.antserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AntserverApplication

fun main(args: Array<String>) {
    runApplication<AntserverApplication>(*args)
}
