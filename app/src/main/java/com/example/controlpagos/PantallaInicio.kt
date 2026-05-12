package com.example.controlpagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import com.example.controlpagos.model.Ingreso
import java.util.Calendar
import java.util.Date

@Composable
fun PantallaInicio(listaCuentas: List<Cuenta>, listaIngresos: List<Ingreso>) {

    val cuentasPorMes = listaCuentas.mapNotNull { cuenta ->
        val fechaDate = parseFechaApp(cuenta.fecha) ?: return@mapNotNull null
        val calendar = Calendar.getInstance().apply { time = fechaDate }
        val mes = calendar.get(Calendar.MONTH)
        val anio = calendar.get(Calendar.YEAR)
        "$anio-$mes" to cuenta
    }.groupBy({ it.first }, { it.second })

    val calendar = Calendar.getInstance()
    val mesActual = calendar.get(Calendar.MONTH)
    val anioActual = calendar.get(Calendar.YEAR)
    val claveActual = "$anioActual-$mesActual"

    val cuentasMesActual = cuentasPorMes[claveActual].orEmpty()
    val totalPendienteMesActual = cuentasMesActual
        .filter { !it.pagada }
        .sumOf { it.monto }
    val totalPagadoMesActual = cuentasMesActual
        .filter { it.pagada }
        .sumOf { it.monto }
    val totalIngresosMesActual = listaIngresos.filter { ingreso ->
        val fecha = parseFechaApp(ingreso.fecha) ?: return@filter false
        val cal = Calendar.getInstance().apply { time = fecha }
        cal.get(Calendar.YEAR) == anioActual && cal.get(Calendar.MONTH) == mesActual
    }.sumOf { it.monto }
    val balanceEstimadoMesActual = totalIngresosMesActual - totalPendienteMesActual
    val resumenQuincenalMesActualEstado = remember(listaCuentas, listaIngresos) {
        resumenQuincenalMesActual(listaCuentas, listaIngresos)
    }
    val periodoActual = remember { periodoQuincenalDe(Date()) }
    val resumenQuincenaActual = resumenQuincenalMesActualEstado.firstOrNull { it.periodo.quincena == periodoActual.quincena }
    val totalCuentasMesActual = cuentasMesActual.size
    val cuentasPagadasMesActual = cuentasMesActual.count { it.pagada }

    fun obtenerNombreMes(clave: String): String {
        val partes = clave.split("-")
        val anio = partes.getOrNull(0) ?: return clave
        val mes = partes.getOrNull(1)?.toIntOrNull() ?: return clave

        val meses = listOf(
            "Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        return "${meses[mes]} $anio"
    }

    fun descomponerClaveMes(clave: String): Pair<Int, Int> {
        val partes = clave.split("-")
        val anio = partes.getOrNull(0)?.toIntOrNull() ?: 0
        val mes = partes.getOrNull(1)?.toIntOrNull() ?: 0
        return anio to mes
    }

    val resumenMensualOrdenado = cuentasPorMes.toList().sortedWith(
        compareByDescending<Pair<String, List<Cuenta>>> { descomponerClaveMes(it.first).first }
            .thenByDescending { descomponerClaveMes(it.first).second }
    )

    val proximosPagos = listaCuentas
        .filter { !it.pagada }
        .sortedBy { parseFechaApp(it.fecha) ?: Date(Long.MAX_VALUE) }
        .take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

        item {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Administra tus pagos fácilmente",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Tablero principal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Resumen del mes actual y la quincena activa en una sola vista.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        MiniIndicadorInicio(
                            modifier = Modifier.weight(1f),
                            titulo = "Cuentas",
                            valor = totalCuentasMesActual.toString(),
                            contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        MiniIndicadorInicio(
                            modifier = Modifier.weight(1f),
                            titulo = "Pagadas",
                            valor = cuentasPagadasMesActual.toString(),
                            contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        MiniIndicadorInicio(
                            modifier = Modifier.weight(1f),
                            titulo = "Pendientes",
                            valor = cuentasMesActual.count { !it.pagada }.toString(),
                            contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${nombreMesEsp(mesActual)} $anioActual • ${etiquetaPeriodoQuincenal(resumenQuincenaActual?.periodo ?: periodoActual)}") },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                            disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    Text(text = "Pendiente del mes actual", style = MaterialTheme.typography.titleMedium)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatearMontoApp(totalPendienteMesActual),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Pagado: ${formatearMontoApp(totalPagadoMesActual)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ingresos del mes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        Text(formatearMontoApp(totalIngresosMesActual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Balance estimado", style = MaterialTheme.typography.labelMedium, color = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                        Text(formatearMontoApp(balanceEstimadoMesActual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resumen quincenal del mes actual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${nombreMesEsp(mesActual)} $anioActual",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (resumenQuincenaActual != null) {
                        Text(
                            text = etiquetaPeriodoQuincenal(resumenQuincenaActual.periodo),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Pendiente", style = MaterialTheme.typography.labelMedium)
                                    Text(formatearMontoApp(resumenQuincenaActual.totalPendiente), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Ingresos", style = MaterialTheme.typography.labelMedium)
                                    Text(formatearMontoApp(resumenQuincenaActual.totalIngresos), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Balance: ${formatearMontoApp(resumenQuincenaActual.balance)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                resumenQuincenalMesActualEstado.forEach { resumen ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (resumen.periodo.quincena == periodoActual.quincena) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = etiquetaQuincenaCorta(resumen.periodo.quincena),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (resumen.periodo.quincena == periodoActual.quincena) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = rangoQuincenalTexto(resumen.periodo.quincena),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (resumen.periodo.quincena == periodoActual.quincena) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatearMontoApp(resumen.totalIngresos),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (resumen.periodo.quincena == periodoActual.quincena) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            Text(
                                text = "Pendiente ${formatearMontoApp(resumen.totalPendiente)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (resumen.periodo.quincena == periodoActual.quincena) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Próximos pagos",
                style = MaterialTheme.typography.titleLarge
            )
        }

        if (proximosPagos.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin próximos pagos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "¡Estás al día!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            item { Spacer(modifier = Modifier.height(10.dp)) }

            items(proximosPagos) { cuenta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = cuenta.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Fecha: ${cuenta.fecha}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Cuenta: ${cuenta.numeroCuenta}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = formatearMontoApp(cuenta.monto),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }


        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Resumen por meses",
                style = MaterialTheme.typography.titleLarge
            )
        }

        resumenMensualOrdenado.forEach { (clave, cuentas) ->

            val totalMes = cuentas.sumOf { it.monto }
            val totalPendiente = cuentas.filter { !it.pagada }.sumOf { it.monto }
            val totalPagado = cuentas.filter { it.pagada }.sumOf { it.monto }

            item {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text = obtenerNombreMes(clave),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Total: ${formatearMontoApp(totalMes)}",
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Pendiente: ${formatearMontoApp(totalPendiente)} | Pagado: ${formatearMontoApp(totalPagado)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        }
    }
}

@Composable
private fun MiniIndicadorInicio(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    contenidoColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = contenidoColor)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contenidoColor)
        }
    }
}

