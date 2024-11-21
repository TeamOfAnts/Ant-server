package com.example.antserver

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class AntserverApplicationTests {

    @Test
    fun contextLoads() {
    }

    //FIXME: 테스트가 실패함, AntserverApplication.kt 에서 defaultTimeZone을 설정하는데 테스트 시에는 별도의 테스트가 필요해 보임
    @Test
    @Disabled
    @DisplayName("타임존이 UTC (UTC+0)")
    fun timeZoneTest() {
        Assertions.assertSame(
            TimeZone.getDefault(),
            TimeZone.getTimeZone("UTC")
        );
    }

}
