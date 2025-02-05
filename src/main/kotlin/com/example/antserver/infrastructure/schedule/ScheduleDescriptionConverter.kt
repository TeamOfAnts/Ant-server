package com.example.antserver.infrastructure.schedule

import com.example.antserver.domain.schedule.ScheduleDescription
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.DayOfWeek
import java.time.LocalDate

@Converter(autoApply = true)
class ScheduleDescriptionConverter : AttributeConverter<ScheduleDescription, String> {
    override fun convertToDatabaseColumn(attribute: ScheduleDescription): String {
        return when (attribute) {
            is ScheduleDescription.Scheduled -> attribute.toString()
            ScheduleDescription.Unscheduled -> "미참여"
        }
    }

    override fun convertToEntityAttribute(dbData: String): ScheduleDescription {
        return if (dbData == "미참여") {
            ScheduleDescription.Unscheduled
        } else {
            val parts = dbData.split(" ")
            val date = LocalDate.parse(parts[0])
            val dayOfWeekKorean = parts[1].removeSurrounding("(", ")")
            val dayOfWeek = koreanToEnglish[dayOfWeekKorean]!!
            val timeOfDay = parts.getOrNull(2)
            ScheduleDescription.Scheduled(date, dayOfWeek, timeOfDay)
        }
    }

    private val koreanToEnglish = mapOf(
        "월" to DayOfWeek.MONDAY,
        "화" to DayOfWeek.TUESDAY,
        "수" to DayOfWeek.WEDNESDAY,
        "목" to DayOfWeek.THURSDAY,
        "금" to DayOfWeek.FRIDAY,
        "토" to DayOfWeek.SATURDAY,
        "일" to DayOfWeek.SUNDAY
    )
}