package com.example.controlpagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class TendenciaMensual(
    val totalMesActual: Double,
    val totalMesAnterior: Double,
    val diferencia: Double,
    val porcentaje: Double
)

@Composable
fun EstadisticasPagos(listaCuentas: List<Cuenta>) {
    var periodoSeleccionado by rememberSaveable { mutableIntStateOf(6) }

    val hoy = remember { Date() }
    val cuentasConFecha = remember(listaCuentas) {
        listaCuentas.mapNotNull { cuenta ->
            val fecha = parseFechaApp(cuenta.fecha) ?: return@mapNotNull null
            cuenta to fecha
        }
    }

    val totalGeneral = remember(listaCuentas) { listaCuentas.sumOf { it.monto } }
    val totalPendienteMonto = remember(listaCuentas) {
        listaCuentas.filter { !it.pagada }.sumOf { it.monto }
    }
    val totalPagadoMonto = remember(listaCuentas) {
        listaCuentas.filter { it.pagada }.sumOf { it.monto }
    }
    val totalVencidas = remember(cuentasConFecha) {
        cuentasConFecha.count { (cuenta, fecha) -> !cuenta.pagada && fecha.before(hoy) }
    }
    val totalPendientes = remember(listaCuentas) { listaCuentas.count { !it.pagada } }
    val promedio = remember(listaCuentas) {
        if (listaCuentas.isEmpty()) 0.0 else totalGeneral / listaCuentas.size
    }

    val totalesMes = remember(listaCuentas, periodoSeleccionado) {
        calcularTotalesMensuales(listaCuentas, periodoSeleccionado)
    }
    val tendencia = remember(totalesMes) { calcularTendenciaMensual(totalesMes) }
    val textoTendencia = if (tendencia.diferencia >= 0) "Sube" else "Baja"
    val colorTendencia = if (tendencia.diferencia >= 0) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadisticas",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("Pendiente", formatearMontoApp(totalPendienteMonto), Modifier.weight(1f))
                KpiMiniCard("Vencidas", totalVencidas.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("Pagado", formatearMontoApp(totalPagadoMonto), Modifier.weight(1f))
                KpiMiniCard("Pendientes", totalPendientes.toString(), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            KpiMiniCard("Promedio", formatearMontoApp(promedio), Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 12).forEach { periodo ->
                    FilterChip(
                        selected = periodoSeleccionado == periodo,
                        onClick = { periodoSeleccionado = periodo },
                        label = { Text("$periodo m") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ultimos $periodoSeleccionado meses",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$textoTendencia vs mes anterior: ${String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(tendencia.porcentaje))}%",
                style = MaterialTheme.typography.bodySmall,
                color = colorTendencia
            )
            Spacer(modifier = Modifier.height(8.dp))
            GraficaBarrasMes(totalesMes)
        }
    }
}

@Composable
private fun KpiMiniCard(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun GraficaBarrasMes(totales: List<Pair<String, Double>>) {
    val maximo = (totales.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
    val indiceActual = totales.lastIndex

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        totales.forEachIndexed { index, (mes, total) ->
            val altura = ((total / maximo) * 100).toFloat().coerceAtLeast(4f)
            val colorBarra = if (index == indiceActual) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.primary
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(altura.dp)
                        .background(
                            color = colorBarra,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = mes, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

internal fun calcularTotalesMensuales(
    cuentas: List<Cuenta>,
    meses: Int,
    baseReferencia: Calendar = Calendar.getInstance()
): List<Pair<String, Double>> {
    val mesesCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    val totalesMap = cuentas.groupBy { cuenta ->
        val fecha = parseFechaApp(cuenta.fecha)
        if (fecha == null) {
            ""
        } else {
            val cal = Calendar.getInstance().apply { time = fecha }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
        }
    }.mapValues { (_, lista) -> lista.sumOf { it.monto } }

    val base = (baseReferencia.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val inicioOffset = (meses - 1).coerceAtLeast(0)
    return (inicioOffset downTo 0).map { offset ->
        val cal = base.clone() as Calendar
        cal.add(Calendar.MONTH, -offset)
        val anio = cal.get(Calendar.YEAR)
        val mes = cal.get(Calendar.MONTH)
        val clave = "$anio-$mes"
        val etiqueta = mesesCortos[mes]
        etiqueta to (totalesMap[clave] ?: 0.0)
    }
}

internal fun calcularTendenciaMensual(totales: List<Pair<String, Double>>): TendenciaMensual {
    val actual = totales.lastOrNull()?.second ?: 0.0
    val anterior = if (totales.size >= 2) totales[totales.lastIndex - 1].second else 0.0
    val diferencia = actual - anterior
    val porcentaje = if (anterior == 0.0) {
        if (actual == 0.0) 0.0 else 100.0
    } else {
        (diferencia / anterior) * 100.0
    }
    return TendenciaMensual(actual, anterior, diferencia, porcentaje)
}

