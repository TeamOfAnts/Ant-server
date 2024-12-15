package com.example.antserver.presentation.poll.dto

import org.springframework.data.domain.Page

data class PageResponse<T>(
    val content: List<T>, // 현재 페이지 데이터
    val totalPages: Int,  // 전체 페이지 수
    val totalElements: Long, // 전체 데이터 개수
    val size: Int,        // 한 페이지의 데이터 수
    val currentPage: Int  // 현재 페이지 번호 (0부터 시작)
) {
    companion object {
        fun <T> from(page: Page<T>): PageResponse<T> {
            return PageResponse(
                content = page.content,
                totalPages = page.totalPages,
                totalElements = page.totalElements,
                size = page.size,
                currentPage = page.number
            )
        }
    }
}