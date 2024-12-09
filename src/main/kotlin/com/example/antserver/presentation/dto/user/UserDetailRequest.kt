package com.example.antserver.presentation.dto.user

import java.util.UUID

data class UserDetailRequest(
    private val userId: UUID,
    private val userName: String
)