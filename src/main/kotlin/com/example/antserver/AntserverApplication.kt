package com.example.antserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
class AntserverApplication

fun main(args: Array<String>) {
    runApplication<AntserverApplication>(*args)
}
