package com.smitestats.datacollectorservice.helpers

import java.time.{ LocalDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter

object TemporalHelpers {
    def getDateTwoHoursPrev: String = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    def getHourTwoHoursPrev: Int = LocalDateTime.now(ZoneOffset.UTC).minusHours(2).format(DateTimeFormatter.ofPattern("HH")).toInt
    def getCurrentMinute: Int = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("mm")).toInt
}
