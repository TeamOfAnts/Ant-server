package com.example.antserver.domain.user

class UserValidator {

    fun validate(userEmail: String, userEmailFromGoogle: String): Boolean {
        return userEmail == userEmailFromGoogle
    }
}