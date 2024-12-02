package com.example.alarmclock

class AlarmClock(val id: Int, val time: String) {
    override fun toString(): String {
        return "Будильник на $time"
    }
}