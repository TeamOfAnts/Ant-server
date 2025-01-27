package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.ScheduleOn
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.DayOfWeek
import java.time.LocalDate

@Converter(autoApply = true)
class ScheduleOnConverter : AttributeConverter<ScheduleOn, String> {
    override fun convertToDatabaseColumn(attribute: ScheduleOn): String? {
        return when (attribute) {
            is ScheduleOn.Scheduled -> attribute.toString()
            ScheduleOn.Unscheduled -> null
        }
    }

    override fun convertToEntityAttribute(dbData: String?): ScheduleOn {
        return if (dbData == null) {
            ScheduleOn.Unscheduled
        } else {
            val parts = dbData.split(" ")
            val date = LocalDate.parse(parts[0])
            val dayOfWeek = DayOfWeek.valueOf(parts[1])
            val timeOfDay = parts.getOrNull(2)
            ScheduleOn.Scheduled(date, dayOfWeek, timeOfDay)
        }
    }
}