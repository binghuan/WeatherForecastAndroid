package com.bh.weatherforecastandroid

import org.junit.Test
import org.junit.Assert.assertEquals
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ExampleUnitTest {
    @Test
    fun dateFormatting_isStable() {
        val date = LocalDate.of(2025, 10, 14)
        val text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        // Format is locale dependent; ensure it's non-empty and contains year
        assert(text.isNotBlank())
        assert(text.contains("2025"))
    }
}