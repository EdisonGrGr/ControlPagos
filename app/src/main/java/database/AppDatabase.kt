package com.example.controlpagos.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.controlpagos.dao.CuentaDao
import com.example.controlpagos.dao.IngresoDao
import com.example.controlpagos.model.Cuenta
import com.example.controlpagos.model.Ingreso

@Database(entities = [Cuenta::class, Ingreso::class], version = 4, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    abstract fun cuentaDao(): CuentaDao

    abstract fun ingresoDao(): IngresoDao

}
