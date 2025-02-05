package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.ScheduleOn
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Instant
import java.time.ZoneId

@Converter(autoApply = true)
class ScheduleOnConverter : AttributeConverter<ScheduleOn, Instant> {
    override fun convertToDatabaseColumn(attribute: ScheduleOn): Instant? {
        return when (attribute) {
            is ScheduleOn.Scheduled -> attribute.date.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant()
            ScheduleOn.Unscheduled -> null
        }
    }

    override fun convertToEntityAttribute(dbData: Instant?): ScheduleOn {
        return if (dbData == null) {
            ScheduleOn.Unscheduled
        } else {
            ScheduleOn.Scheduled(dbData.atZone(ZoneId.of("Asia/Seoul")).toLocalDate())
        }
    }
}