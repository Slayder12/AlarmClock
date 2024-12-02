package com.example.alarmclock

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AlarmViewModel: ViewModel() {
    val alarmLiveData: MutableLiveData<MutableList<AlarmClock>> = MutableLiveData(mutableListOf())
}