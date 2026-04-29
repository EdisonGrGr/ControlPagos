package com.example.controlpagos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import com.example.controlpagos.database.AppDatabase
import com.example.controlpagos.ui.theme.ControlPagosTheme
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "control_pagos_db"

        )
            .fallbackToDestructiveMigration()
            .build()

        setContent {
            ControlPagosTheme {
                MenuPrincipal(db.cuentaDao(), db.ingresoDao())
            }
        }
    }
}
