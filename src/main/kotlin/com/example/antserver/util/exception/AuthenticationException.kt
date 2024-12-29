package com.example.antserver.util.exception

open class AuthenticationException (
    override val message: String
): RuntimeException(message)