package com.vikasshelar.lumenx.feature.timer

enum class TimerOption(
    val seconds: Int,
    val label: String,
) {
    Off(0, "Off"),
    OneMinute(60, "1m"),
    FiveMinutes(300, "5m"),
    FifteenMinutes(900, "15m"),
}
