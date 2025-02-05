package com.example.antserver

import com.example.antserver.testconfig.TestcontainersConfig
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import

@Import(TestcontainersConfig::class)
class AntserverApplicationTests {

    @Test
    fun contextLoads() {
    }

}
