package com.example.antserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AntserverApplication

fun main(args: Array<String>) {
    //TODO: 아래 처럼 main 에서 무언가 설정하는 것은 어색한 방법임 더 좋은 방법이 있다면 변경 할 것
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<AntserverApplication>(*args)
    println("current application timezone is : " + TimeZone.getDefault().toZoneId())
}
