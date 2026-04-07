package com.example.controlpagos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas")
data class Cuenta(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val numeroCuenta: String,
    val monto: Double,
    val fecha: String
)
