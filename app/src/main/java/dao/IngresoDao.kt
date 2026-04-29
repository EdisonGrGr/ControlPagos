package com.example.controlpagos.dao

import androidx.room.*
import com.example.controlpagos.model.Ingreso

@Dao
interface IngresoDao {

    @Insert
    suspend fun insertarIngreso(ingreso: Ingreso)

    @Query("SELECT * FROM ingresos")
    suspend fun obtenerIngresos(): List<Ingreso>

    @Delete
    suspend fun eliminarIngreso(ingreso: Ingreso)

    @Update
    suspend fun actualizarIngreso(ingreso: Ingreso)
}

