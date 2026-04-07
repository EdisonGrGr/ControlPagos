package com.example.controlpagos

import com.example.controlpagos.model.Cuenta
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun calcularTotalesMensuales_devuelvePeriodoSolicitado() {
        val base = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.APRIL)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val cuentas = listOf(
            Cuenta(nombre = "Luz", numeroCuenta = "1", monto = 100.0, fecha = "05/04/2026"),
            Cuenta(nombre = "Agua", numeroCuenta = "2", monto = 80.0, fecha = "10/03/2026"),
            Cuenta(nombre = "Internet", numeroCuenta = "3", monto = 50.0, fecha = "15/02/2026")
        )

        val resultado = calcularTotalesMensuales(cuentas, meses = 3, baseReferencia = base)

        assertEquals(3, resultado.size)
        assertEquals("Feb", resultado[0].first)
        assertEquals(50.0, resultado[0].second, 0.001)
        assertEquals("Mar", resultado[1].first)
        assertEquals(80.0, resultado[1].second, 0.001)
        assertEquals("Abr", resultado[2].first)
        assertEquals(100.0, resultado[2].second, 0.001)
    }

    @Test
    fun calcularTendenciaMensual_calculaPorcentajeCorrecto() {
        val totales = listOf(
            "Mar" to 100.0,
            "Abr" to 120.0
        )

        val tendencia = calcularTendenciaMensual(totales)

        assertEquals(120.0, tendencia.totalMesActual, 0.001)
        assertEquals(100.0, tendencia.totalMesAnterior, 0.001)
        assertEquals(20.0, tendencia.diferencia, 0.001)
        assertEquals(20.0, tendencia.porcentaje, 0.001)
    }

    @Test
    fun calcularTendenciaMensual_manejaDivisionPorCero() {
        val totales = listOf("Abr" to 50.0)

        val tendencia = calcularTendenciaMensual(totales)

        assertEquals(50.0, tendencia.totalMesActual, 0.001)
        assertEquals(0.0, tendencia.totalMesAnterior, 0.001)
        assertEquals(100.0, tendencia.porcentaje, 0.001)
    }

    @Test
    fun cuentasToCsv_y_csvToCuentas_roundTripBasico() {
        val cuentas = listOf(
            Cuenta(nombre = "Luz", numeroCuenta = "A-1", monto = 123.45, fecha = "01/04/2026"),
            Cuenta(nombre = "Internet", numeroCuenta = "B-2", monto = 67.89, fecha = "15/04/2026")
        )

        val csv = cuentasToCsv(cuentas)
        val parseadas = csvToCuentas(csv)

        assertEquals(2, parseadas.size)
        assertEquals("Luz", parseadas[0].nombre)
        assertEquals("A-1", parseadas[0].numeroCuenta)
        assertEquals(123.45, parseadas[0].monto, 0.001)
        assertEquals("01/04/2026", parseadas[0].fecha)
    }

    @Test
    fun csvToCuentas_ignoraFilasInvalidas() {
        val csv = """
            nombre,numeroCuenta,monto,fecha
            Luz,1,100.0,05/04/2026
            Agua,2,-10.0,10/04/2026
            Invalida,3,20.0,99/99/9999
        """.trimIndent()

        val parseadas = csvToCuentas(csv)

        assertEquals(1, parseadas.size)
        assertEquals("Luz", parseadas[0].nombre)
    }
}