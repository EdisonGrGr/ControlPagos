package com.example.controlpagos

import com.example.controlpagos.dao.CuentaDao
import com.example.controlpagos.model.Cuenta

interface CuentaRepository {
    suspend fun obtenerCuentas(): List<Cuenta>
    suspend fun insertarCuenta(cuenta: Cuenta)
    suspend fun actualizarCuenta(cuenta: Cuenta)
    suspend fun eliminarCuenta(cuenta: Cuenta)
}

class CuentaRepositoryImpl(
    private val cuentaDao: CuentaDao
) : CuentaRepository {

    override suspend fun obtenerCuentas(): List<Cuenta> = cuentaDao.obtenerCuentas()

    override suspend fun insertarCuenta(cuenta: Cuenta) {
        cuentaDao.insertarCuenta(cuenta)
    }

    override suspend fun actualizarCuenta(cuenta: Cuenta) {
        cuentaDao.actualizarCuenta(cuenta)
    }

    override suspend fun eliminarCuenta(cuenta: Cuenta) {
        cuentaDao.eliminarCuenta(cuenta)
    }
}

