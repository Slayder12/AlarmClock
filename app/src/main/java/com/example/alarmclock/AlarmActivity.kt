package com.example.alarmclock


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import kotlin.system.exitProcess

class AlarmActivity : AppCompatActivity() {

    private lateinit var stopAlarmBTN: Button
    private lateinit var imageViewIV: ImageView
    private lateinit var toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)

        toolbar = findViewById(R.id.toolbar)
        title = ""
        setSupportActionBar(toolbar)

        imageViewIV = findViewById(R.id.imageViewIV)
        imageViewIV.setImageResource(R.drawable.alarm)

        stopAlarmBTN = findViewById(R.id.stopAlarmBTN)
        stopAlarmBTN.setOnClickListener{
            finish()
            exitProcess(0)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.exitMenu) {
            finishAffinity()
            exitProcess(0)
        }
        return super.onOptionsItemSelected(item)
    }
}