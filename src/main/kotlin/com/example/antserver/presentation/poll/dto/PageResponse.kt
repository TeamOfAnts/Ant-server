package com.example.antserver.presentation.poll.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val contents: List<T>,
    val totalCount: Long
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                contents = page.content,
                totalCount = page.totalElements
            )
        }
    }
}