package com.example.alarmclock

import android.app.AlarmManager
import android.app.AlarmManager.RTC_WAKEUP
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    private var calendar: Calendar? = null
    private var materialTimePicker: MaterialTimePicker? = null

    private lateinit var alarmBTN: Button

    private var alarmIdCounter: Int = 0
    private val sharedPreferences by lazy { getSharedPreferences("alarms_prefs", Context.MODE_PRIVATE) }

    private val alarmList: MutableList<AlarmClock> = mutableListOf()
    private var adapter: ArrayAdapter<AlarmClock>? = null
    private lateinit var listViewLV: ListView

    private lateinit var alarmLiveData: AlarmViewModel

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmLiveData = ViewModelProvider(this)[AlarmViewModel::class.java]

        toolbar = findViewById(R.id.toolbar)
        title = ""
        setSupportActionBar(toolbar)
        listViewLV = findViewById(R.id.listViewLV)
        alarmBTN = findViewById(R.id.alarmBTN)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, alarmList)
        listViewLV.adapter = adapter

        alarmLiveData.alarmLiveData.observe(this, Observer { alarmClocks ->
            adapter?.clear()
            adapter?.addAll(alarmClocks)
            adapter?.notifyDataSetChanged()
        })

        alarmBTN.setOnClickListener{
            materialTimePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Выберите время будильника")
                .build()
            materialTimePicker!!.addOnPositiveButtonClickListener{
                calendar = Calendar.getInstance()
                calendar?.set(Calendar.SECOND, 0)
                calendar?.set(Calendar.MILLISECOND, 0)
                calendar?.set(Calendar.MINUTE, materialTimePicker!!.minute)
                calendar?.set(Calendar.HOUR_OF_DAY, materialTimePicker!!.hour)

                val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

                val alarmId = getNextAlarmId()
                alarmManager.setExact(
                    RTC_WAKEUP,
                    calendar?.timeInMillis!!,
                    getAlarmPendingIntent(alarmId)!!
                )

                val alarmClock = AlarmClock(alarmId, dateFormat.format(calendar!!.time))

                alarmList.add(alarmClock)
                adapter!!.notifyDataSetChanged()
                saveAlarmId(alarmId)

                val currentList = alarmLiveData.alarmLiveData.value ?: mutableListOf()
                currentList.add(alarmClock)
                alarmLiveData.alarmLiveData.value = currentList

                Toast.makeText(this, "Будильник установлен на ${dateFormat.format(calendar!!.time)}", Toast.LENGTH_LONG).show()
            }
            materialTimePicker!!.show(supportFragmentManager, "tag_picker")
        }

        listViewLV.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                showDeleteDialog(position)
            }

    }

    private fun getAlarmPendingIntent(alarmId: Int): PendingIntent? {
        val intent = Intent(this, AlarmReceiver::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getNextAlarmId(): Int {
        alarmIdCounter++
        return alarmIdCounter
    }

    private fun saveAlarmId(alarmId: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("alarmIdCounter", alarmIdCounter)
        val ids = sharedPreferences.getStringSet("alarmIds", mutableSetOf())?.toMutableSet()
        ids?.add(alarmId.toString())
        editor.putStringSet("alarmIds", ids)
        editor.apply()
    }


    private fun showDeleteDialog(position: Int) {
        val alarm = adapter!!.getItem(position)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Хотите удалить будильник на ${alarm!!.time}?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Да") { dialog, _ ->
                deleteAlarm(alarm.id)
                val currentList = alarmLiveData.alarmLiveData.value ?: mutableListOf()
                currentList.remove(alarm)
                alarmList.remove(alarm)
                alarmLiveData.alarmLiveData.value = currentList
                adapter?.notifyDataSetChanged()
                dialog.dismiss()
            }
        builder.setNegativeButton("Нет") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deleteAlarm(alarmId: Int) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val pendingIntent = getAlarmPendingIntent(alarmId)
        alarmManager.cancel(pendingIntent!!)
        pendingIntent.cancel()

        val editor = sharedPreferences.edit()
        val ids = sharedPreferences.getStringSet("alarmIds", mutableSetOf())?.toMutableSet()
        ids?.remove(alarmId.toString())
        editor.putStringSet("alarmIds", ids)
        editor.apply()

        Toast.makeText(this, "Будильник с ID $alarmId удален", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        alarmIdCounter = sharedPreferences.getInt("alarmIdCounter", 0)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.exitMenu) {
            finishAffinity()
        }
        return super.onOptionsItemSelected(item)
    }
}