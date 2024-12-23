package com.example.antserver.util.response

sealed class Status {
    data object BadRequest : Status()
    data object Unauthorized : Status()
    data object NotFound : Status()
    data object ServerError : Status()
}