package com.example.controlpagos

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioPagos(cuentas: List<Cuenta>) {

    val calendar = Calendar.getInstance()
    val hoyDia = calendar.get(Calendar.DAY_OF_MONTH)
    val hoyMes = calendar.get(Calendar.MONTH)
    val hoyAnio = calendar.get(Calendar.YEAR)
    var mesMostrado by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var anioMostrado by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var diaSeleccionado by remember { mutableStateOf<Int?>(null) }

    val cuentasPorDia = remember(cuentas) {
        cuentas.mapNotNull { cuenta ->
            val fecha = parseFechaApp(cuenta.fecha) ?: return@mapNotNull null
            val cal = Calendar.getInstance().apply { time = fecha }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)) to cuenta
        }.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )
    }

    val calendarMes = Calendar.getInstance().apply {
        set(Calendar.YEAR, anioMostrado)
        set(Calendar.MONTH, mesMostrado)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val primerDiaSemana = calendarMes.get(Calendar.DAY_OF_WEEK) - 1
    val diasEnMes = calendarMes.getActualMaximum(Calendar.DAY_OF_MONTH)

    val pagosDelMes = remember(cuentasPorDia, mesMostrado, anioMostrado) {
        cuentasPorDia.keys
            .filter { it.first == anioMostrado && it.second == mesMostrado }
            .map { it.third }
            .toSet()
    }

    val pagosDelDiaSeleccionado = remember(cuentasPorDia, mesMostrado, anioMostrado, diaSeleccionado) {
        diaSeleccionado?.let { dia ->
            cuentasPorDia[Triple(anioMostrado, mesMostrado, dia)].orEmpty()
        }.orEmpty()
    }

    val cuentasMesMostrado = remember(cuentasPorDia, mesMostrado, anioMostrado) {
        cuentasPorDia.filter { (clave, _) -> clave.first == anioMostrado && clave.second == mesMostrado }
            .values
            .flatten()
    }
    val totalMontoMesMostrado = remember(cuentasMesMostrado) { cuentasMesMostrado.sumOf { it.monto } }
    val diasConPagoMesMostrado = pagosDelMes.size
    val cuentasConPagoMesMostrado = cuentasMesMostrado.size
    val cuentasPendientesMesMostrado = remember(cuentasMesMostrado) { cuentasMesMostrado.count { !it.pagada } }
    val quincenaActual = periodoQuincenalDe(Date())

    val diasSemana = listOf("D", "L", "M", "M", "J", "V", "S")
    val nombresMeses = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Calendario de pagos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Revisa tus pagos por mes, día y quincena.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${nombresMeses[mesMostrado]} $anioMostrado",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pagos visibles y días con movimiento en el mes mostrado.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            CalendarioMetricCard(
                                modifier = Modifier.weight(1f),
                                titulo = "Pagos",
                                valor = cuentasConPagoMesMostrado.toString(),
                                contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            CalendarioMetricCard(
                                modifier = Modifier.weight(1f),
                                titulo = "Pendientes",
                                valor = cuentasPendientesMesMostrado.toString(),
                                contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            CalendarioMetricCard(
                                modifier = Modifier.weight(1f),
                                titulo = "Monto",
                                valor = formatearMontoApp(totalMontoMesMostrado),
                                contenidoColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Quincena actual: ${etiquetaQuincenaCorta(quincenaActual.quincena)}") },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )

                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Días con pago: $diasConPagoMesMostrado") },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                    disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (mesMostrado == 0) {
                                mesMostrado = 11
                                anioMostrado -= 1
                            } else {
                                mesMostrado -= 1
                            }
                        }
                    ) {
                        Text("◀ Mes")
                    }

                    OutlinedButton(
                        onClick = {
                            if (mesMostrado == 11) {
                                mesMostrado = 0
                                anioMostrado += 1
                            } else {
                                mesMostrado += 1
                            }
                        }
                    ) {
                        Text("Mes ▶")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LeyendaCalendarioPagos(hayPagosEnMes = pagosDelMes.isNotEmpty())

                Spacer(modifier = Modifier.height(8.dp))

                // Encabezado de semana y grilla mensual fija.
                Row(modifier = Modifier.fillMaxWidth()) {
                    diasSemana.forEach {
                        Text(
                            text = it,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    items(primerDiaSemana) {
                        Box(modifier = Modifier.size(40.dp))
                    }

                    items((1..diasEnMes).toList()) { dia ->
                        val tienePago = dia in pagosDelMes
                        val esHoy = dia == hoyDia && mesMostrado == hoyMes && anioMostrado == hoyAnio

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(40.dp)
                                .then(
                                    if (tienePago) Modifier.clickable { diaSeleccionado = dia }
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tienePago) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = if (diaSeleccionado == dia) {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .then(
                                            if (esHoy) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.tertiary,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dia.toString(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (esHoy) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                    modifier = if (esHoy) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier.size(34.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dia.toString(),
                                            fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (diaSeleccionado != null && pagosDelDiaSeleccionado.isNotEmpty()) {
        ModalBottomSheet(onDismissRequest = { diaSeleccionado = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Pagos del ${diaSeleccionado} de ${nombresMeses[mesMostrado]}",
                    style = MaterialTheme.typography.titleLarge
                )

                val totalDelDia = pagosDelDiaSeleccionado.sumOf { it.monto }
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Total del día: ${formatearMontoApp(totalDelDia)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${pagosDelDiaSeleccionado.size} pago(s) en este día",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                pagosDelDiaSeleccionado.forEach { cuenta ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cuenta.nombre,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = cuenta.numeroCuenta,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = formatearMontoApp(cuenta.monto),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LeyendaCalendarioPagos(hayPagosEnMes: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeyendaItem(color = MaterialTheme.colorScheme.primary, texto = "Con pago")
        LeyendaItem(color = MaterialTheme.colorScheme.surfaceVariant, texto = "Sin pago")
    }

    if (!hayPagosEnMes) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Sin pagos registrados en este mes",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun CalendarioMetricCard(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String,
    contenidoColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = contenidoColor)
            Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contenidoColor)
        }
    }
}

