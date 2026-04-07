package com.example.controlpagos.dao

import androidx.room.*
import com.example.controlpagos.model.Cuenta

@Dao
interface CuentaDao {

    @Insert
    suspend fun insertarCuenta(cuenta: Cuenta)

    @Query("SELECT * FROM cuentas")
    suspend fun obtenerCuentas(): List<Cuenta>

    @Delete
    suspend fun eliminarCuenta(cuenta: Cuenta)

    @Update
    suspend fun actualizarCuenta(cuenta: Cuenta)
}
