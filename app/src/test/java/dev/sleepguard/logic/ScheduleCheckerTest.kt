package dev.sleepguard.logic

import dev.sleepguard.model.SleepSchedule
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime

class ScheduleCheckerTest {
    private fun active(s: SleepSchedule, time: String) = ScheduleChecker.isActive(s, LocalTime.parse(time))
    @Test fun sameDayIncludesStartAndExcludesEnd() {
        val s = SleepSchedule(9 * 60, 17 * 60)
        assertFalse(active(s, "08:59")); assertTrue(active(s, "09:00"))
        assertTrue(active(s, "12:30")); assertTrue(active(s, "16:59")); assertFalse(active(s, "17:00"))
        assertFalse(active(s, "18:00"))
    }
    @Test fun overnightCrossesMidnight() {
        val s = SleepSchedule()
        assertFalse(active(s, "23:29")); assertTrue(active(s, "23:30")); assertTrue(active(s, "00:00"))
        assertTrue(active(s, "07:59")); assertFalse(active(s, "08:00")); assertFalse(active(s, "12:00"))
    }
    @Test fun equalTimesEmptyUnlessAllDay() {
        assertFalse(active(SleepSchedule(0, 0), "00:00"))
        assertFalse(active(SleepSchedule(0, 0), "12:00"))
        assertTrue(active(SleepSchedule(0, 0, true), "12:00"))
    }
}
