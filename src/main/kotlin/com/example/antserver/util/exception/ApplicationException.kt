package com.example.antserver.util.exception

import com.example.antserver.util.response.Status

open class ApplicationException(
    val status: Status,
    val serverMessage: String?,
    override val message: String?
    ): RuntimeException(message)