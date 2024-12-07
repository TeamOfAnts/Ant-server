package com.example.antserver

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
class AntserverApplicationTests {

    @Test
    fun contextLoads() {
    }

}
