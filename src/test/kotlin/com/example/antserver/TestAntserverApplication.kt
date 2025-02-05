package com.example.antserver

import com.example.antserver.testconfig.TestcontainersConfig
import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<AntserverApplication>().with(TestcontainersConfig::class).run(*args)
}
