package com.example.controlpagos.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingresos")
data class Ingreso(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concepto: String,
    val monto: Double,
    val fecha: String,
    val categoria: String = "General",
    val nota: String? = null
)

