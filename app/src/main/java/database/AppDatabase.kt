package com.example.controlpagos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.controlpagos.dao.CuentaDao
import com.example.controlpagos.model.Cuenta

@Database(entities = [Cuenta::class], version = 3, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    abstract fun cuentaDao(): CuentaDao

}
