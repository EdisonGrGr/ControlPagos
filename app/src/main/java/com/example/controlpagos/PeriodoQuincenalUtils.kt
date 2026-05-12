package com.example.controlpagos

import com.example.controlpagos.model.Cuenta
import com.example.controlpagos.model.Ingreso
import java.util.Calendar
import java.util.Date

data class PeriodoQuincenal(
    val anio: Int,
    val mes: Int,
    val quincena: Int
)

data class ResumenQuincenal(
    val periodo: PeriodoQuincenal,
    val totalPagos: Double,
    val totalPendiente: Double,
    val totalPagado: Double,
    val totalIngresos: Double
) {
    val balance: Double
        get() = totalIngresos - totalPendiente
}

private val MESES_ES = listOf(
    "Enero", "Febrero", "Marzo", "Abril",
    "Mayo", "Junio", "Julio", "Agosto",
    "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

fun periodoQuincenalDe(fecha: Date): PeriodoQuincenal {
    val calendario = Calendar.getInstance().apply { time = fecha }
    val quincena = if (calendario.get(Calendar.DAY_OF_MONTH) <= 15) 1 else 2
    return PeriodoQuincenal(
        anio = calendario.get(Calendar.YEAR),
        mes = calendario.get(Calendar.MONTH),
        quincena = quincena
    )
}

fun periodoQuincenalDe(fechaTexto: String): PeriodoQuincenal? {
    val fecha = parseFechaApp(fechaTexto) ?: return null
    return periodoQuincenalDe(fecha)
}

fun nombreMesEsp(mes: Int): String = MESES_ES.getOrElse(mes) { "Mes ${mes + 1}" }

fun etiquetaQuincenaCorta(quincena: Int): String = when (quincena) {
    1 -> "1ra quincena"
    else -> "2da quincena"
}

fun rangoQuincenalTexto(quincena: Int): String = when (quincena) {
    1 -> "1 al 15"
    else -> "16 al fin de mes"
}

fun etiquetaPeriodoQuincenal(periodo: PeriodoQuincenal): String {
    return "${etiquetaQuincenaCorta(periodo.quincena)} - ${nombreMesEsp(periodo.mes)} ${periodo.anio}"
}

fun resumenQuincenalMes(
    cuentas: List<Cuenta>,
    ingresos: List<Ingreso>,
    anio: Int,
    mes: Int
): List<ResumenQuincenal> {
    return listOf(1, 2).map { quincena ->
        val cuentasQuincena = cuentas.mapNotNull { cuenta ->
            val periodo = periodoQuincenalDe(cuenta.fecha) ?: return@mapNotNull null
            if (periodo.anio == anio && periodo.mes == mes && periodo.quincena == quincena) cuenta else null
        }

        val ingresosQuincena = ingresos.mapNotNull { ingreso ->
            val periodo = periodoQuincenalDe(ingreso.fecha) ?: return@mapNotNull null
            if (periodo.anio == anio && periodo.mes == mes && periodo.quincena == quincena) ingreso else null
        }

        val totalPagos = cuentasQuincena.sumOf { it.monto }
        val totalPendiente = cuentasQuincena.filter { !it.pagada }.sumOf { it.monto }
        val totalPagado = cuentasQuincena.filter { it.pagada }.sumOf { it.monto }
        val totalIngresos = ingresosQuincena.sumOf { it.monto }

        ResumenQuincenal(
            periodo = PeriodoQuincenal(anio = anio, mes = mes, quincena = quincena),
            totalPagos = totalPagos,
            totalPendiente = totalPendiente,
            totalPagado = totalPagado,
            totalIngresos = totalIngresos
        )
    }
}

fun resumenQuincenalMesActual(
    cuentas: List<Cuenta>,
    ingresos: List<Ingreso>,
    baseReferencia: Calendar = Calendar.getInstance()
): List<ResumenQuincenal> {
    return resumenQuincenalMes(
        cuentas = cuentas,
        ingresos = ingresos,
        anio = baseReferencia.get(Calendar.YEAR),
        mes = baseReferencia.get(Calendar.MONTH)
    )
}

