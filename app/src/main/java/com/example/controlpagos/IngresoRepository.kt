package com.example.controlpagos

import com.example.controlpagos.dao.IngresoDao
import com.example.controlpagos.model.Ingreso

interface IngresoRepository {
    suspend fun obtenerIngresos(): List<Ingreso>
    suspend fun insertarIngreso(ingreso: Ingreso)
    suspend fun actualizarIngreso(ingreso: Ingreso)
    suspend fun eliminarIngreso(ingreso: Ingreso)
}

class IngresoRepositoryImpl(
    private val ingresoDao: IngresoDao
) : IngresoRepository {

    override suspend fun obtenerIngresos(): List<Ingreso> = ingresoDao.obtenerIngresos()

    override suspend fun insertarIngreso(ingreso: Ingreso) {
        ingresoDao.insertarIngreso(ingreso)
    }

    override suspend fun actualizarIngreso(ingreso: Ingreso) {
        ingresoDao.actualizarIngreso(ingreso)
    }

    override suspend fun eliminarIngreso(ingreso: Ingreso) {
        ingresoDao.eliminarIngreso(ingreso)
    }
}

