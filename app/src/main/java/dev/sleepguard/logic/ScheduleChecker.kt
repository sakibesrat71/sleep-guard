package dev.sleepguard.logic

import dev.sleepguard.model.SleepSchedule
import java.time.LocalTime

object ScheduleChecker {
    /** Start inclusive, end exclusive. Equal start/end is empty; use allDay for 24 hours. */
    fun isActive(schedule: SleepSchedule, time: LocalTime): Boolean {
        if (schedule.allDay) return true
        val now = time.hour * 60 + time.minute
        val start = schedule.startMinute
        val end = schedule.endMinute
        return if (start < end) now >= start && now < end
        else if (start > end) now >= start || now < end
        else false
    }
}
