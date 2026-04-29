package com.example.controlpagos

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
// Note: specific material icon imports removed to avoid unresolved-symbol issues in some projects.
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import com.example.controlpagos.model.Ingreso
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
// AlertDialog/TextButton no se usan ahora (reemplazados por ModalBottomSheet)
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.mutableStateOf

data class TendenciaMensual(
    val totalMesActual: Double,
    val totalMesAnterior: Double,
    val diferencia: Double,
    val porcentaje: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasPagos(listaCuentas: List<Cuenta>, listaIngresos: List<Ingreso> = emptyList()) {
    var periodoSeleccionado by rememberSaveable { mutableIntStateOf(6) }
    var mesSeleccionadoClave by remember { mutableStateOf<Pair<String,String>?>(null) }

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
    val totalIngresosMonto = remember(listaIngresos) { listaIngresos.sumOf { it.monto } }
    val totalIngresosMesActual = remember(listaIngresos) { totalIngresosDelMesActual(listaIngresos) }
    val totalVencidas = remember(cuentasConFecha) {
        cuentasConFecha.count { (cuenta, fecha) -> !cuenta.pagada && fecha.before(hoy) }
    }
    val totalPendientes = remember(listaCuentas) { listaCuentas.count { !it.pagada } }
    val promedio = remember(listaCuentas) {
        if (listaCuentas.isEmpty()) 0.0 else totalGeneral / listaCuentas.size
    }

    val totalesPagosMes = remember(listaCuentas, periodoSeleccionado) {
        calcularTotalesMensuales(listaCuentas, periodoSeleccionado)
    }
    val totalesIngresosMes = remember(listaIngresos, periodoSeleccionado) {
        calcularTotalesMensualesIngresos(listaIngresos, periodoSeleccionado)
    }
    // calcular totales netos (ingresos - pagos) por mes
    val totalesNetosMes = remember(totalesPagosMes, totalesIngresosMes) {
        totalesIngresosMes.mapIndexed { index, pair ->
            val mes = pair.first
            val ingreso = pair.second
            val pago = if (index < totalesPagosMes.size) totalesPagosMes[index].second else 0.0
            mes to (ingreso - pago)
        }
    }
    val tendencia = remember(totalesNetosMes) { calcularTendenciaMensual(totalesNetosMes) }
    // mostrar tendencia: si sube -> color positivo (primary/tertiary), si baja -> color de error
    val textoTendencia = if (tendencia.diferencia >= 0) "Sube" else "Baja"
    val colorTendencia = if (tendencia.diferencia >= 0) {
        // color positivo (usar terciario si está disponible, si no primary)
        MaterialTheme.colorScheme.tertiary.takeIf { it != MaterialTheme.colorScheme.background } ?: MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
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

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                KpiMiniCard("Ingresos", formatearMontoApp(totalIngresosMonto), Modifier.weight(1f))
                KpiMiniCard("Ingresos mes", formatearMontoApp(totalIngresosMesActual), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            KpiMiniCard("Balance (mes)", formatearMontoApp(totalIngresosMesActual - totalPendienteMonto), Modifier.fillMaxWidth())

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

            // Leyenda (Ingresos / Pagos)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ingresos", style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pagos", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Ultimos $periodoSeleccionado meses (neto ingresos - pagos)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$textoTendencia vs mes anterior: ${String.format(Locale.getDefault(), "%.1f", kotlin.math.abs(tendencia.porcentaje))}%",
                style = MaterialTheme.typography.bodySmall,
                color = colorTendencia
            )
            Spacer(modifier = Modifier.height(8.dp))
            val mesesClaves = generarClavesMensuales(periodoSeleccionado)
            GraficaComparativaMes(totalesPagosMes, totalesIngresosMes, mesesClaves) { clave, label, index ->
                mesSeleccionadoClave = label to clave
            }

            // Mostrar detalles en Bottom Sheet cuando se selecciona un mes
            mesSeleccionadoClave?.let { (label, clave) ->
                val partes = clave.split("-")
                val anio = partes.getOrNull(0)?.toIntOrNull()
                val mes = partes.getOrNull(1)?.toIntOrNull()
                val pagosDetalle = if (anio != null && mes != null) {
                    listaCuentas.filter { cuenta ->
                        val fecha = parseFechaApp(cuenta.fecha) ?: return@filter false
                        val cal = Calendar.getInstance().apply { time = fecha }
                        cal.get(Calendar.YEAR) == anio && cal.get(Calendar.MONTH) == mes
                    }
                } else emptyList()

                val ingresosDetalle = if (anio != null && mes != null) {
                    listaIngresos.filter { ingreso ->
                        val fecha = parseFechaApp(ingreso.fecha) ?: return@filter false
                        val cal = Calendar.getInstance().apply { time = fecha }
                        cal.get(Calendar.YEAR) == anio && cal.get(Calendar.MONTH) == mes
                    }
                } else emptyList()

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { mesSeleccionadoClave = null },
                    sheetState = sheetState
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Detalle: $label", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ingresos:", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (ingresosDetalle.isEmpty()) Text("— Sin ingresos en este mes —", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ingresosDetalle.forEach { i -> Text("• ${i.concepto}: ${formatearMontoApp(i.monto)}") }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Pagos:", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (pagosDetalle.isEmpty()) Text("— Sin pagos en este mes —", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        pagosDetalle.forEach { p -> Text("• ${p.nombre}: ${formatearMontoApp(p.monto)}") }
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMiniCard(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(text = titulo, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = valor, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun maximoComparativo(pagos: List<Pair<String, Double>>, ingresos: List<Pair<String, Double>>): Double {
    val maxPago = pagos.maxOfOrNull { it.second } ?: 0.0
    val maxIngreso = ingresos.maxOfOrNull { it.second } ?: 0.0
    return (maxPago.coerceAtLeast(maxIngreso)).coerceAtLeast(1.0)
}

@Composable
private fun GraficaComparativaMes(
    pagos: List<Pair<String, Double>>,
    ingresos: List<Pair<String, Double>>,
    mesesClaves: List<Pair<String, String>>,
    onMesClick: (clave: String, label: String, index: Int) -> Unit = { _, _, _ -> }
) {
    val count = pagos.size.coerceAtLeast(ingresos.size)
    val alturaMaxima = 96.dp
    val maximo = maximoComparativo(pagos, ingresos)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        for (index in 0 until count) {
            val mes = pagos.getOrNull(index)?.first ?: ingresos.getOrNull(index)?.first ?: ""
            val clave = mesesClaves.getOrNull(index)?.second ?: ""
            val pago = pagos.getOrNull(index)?.second ?: 0.0
            val ingreso = ingresos.getOrNull(index)?.second ?: 0.0

            val proporcionPago = (pago / maximo).toFloat().coerceIn(0f, 1f)
            val proporcionIngreso = (ingreso / maximo).toFloat().coerceIn(0f, 1f)

            val proporcionPagoAnim by animateFloatAsState(
                targetValue = proporcionPago.coerceAtLeast(0.02f),
                animationSpec = tween(durationMillis = 650),
                label = "barra_pago_${mes}_altura"
            )
            val proporcionIngresoAnim by animateFloatAsState(
                targetValue = proporcionIngreso.coerceAtLeast(0.02f),
                animationSpec = tween(durationMillis = 650),
                label = "barra_ingreso_${mes}_altura"
            )

            val alturaPago = alturaMaxima * proporcionPagoAnim
            val alturaIngreso = alturaMaxima * proporcionIngresoAnim

            Column(
                modifier = Modifier.weight(1f).clickable { onMesClick(clave, mes, index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // valores encima si espacio
                Text(text = formatearMontoApp(ingreso), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(alturaIngreso)
                            .background(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // mostrar monto dentro si suficientemente grande
                        if (proporcionIngresoAnim >= 0.32f) {
                            Text(formatearMontoApp(ingreso), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(alturaPago)
                            .background(color = MaterialTheme.colorScheme.error, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (proporcionPagoAnim >= 0.32f) {
                            Text(formatearMontoApp(pago), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = mes, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

internal fun generarClavesMensuales(
    meses: Int,
    baseReferencia: Calendar = Calendar.getInstance()
): List<Pair<String, String>> {
    val mesesCortos = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    val base = (baseReferencia.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val inicioOffset = (meses - 1).coerceAtLeast(0)
    return (inicioOffset downTo 0).map { offset ->
        val cal = base.clone() as Calendar
        cal.add(Calendar.MONTH, -offset)
        val anio = cal.get(Calendar.YEAR)
        val mes = cal.get(Calendar.MONTH)
        val clave = "$anio-$mes"
        val etiqueta = mesesCortos[mes]
        etiqueta to clave
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

