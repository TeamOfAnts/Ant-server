package com.example.antserver

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<AntserverApplication>().with(TestcontainersConfiguration::class).run(*args)
}
