package com.example.antserver.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class AggregateRoot {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = Instant.now()

    @LastModifiedDate
    @Column
    var updatedAt: Instant? = null
}