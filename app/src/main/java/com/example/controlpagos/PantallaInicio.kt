package com.example.controlpagos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import com.example.controlpagos.model.Ingreso
import java.util.*

@Composable
fun PantallaInicio(listaCuentas: List<Cuenta>, listaIngresos: List<Ingreso>) {

    val cuentasPorMes = listaCuentas.groupBy { cuenta ->
        val fechaDate = parseFechaApp(cuenta.fecha)
        val calendar = Calendar.getInstance()
        if (fechaDate != null) {
            calendar.time = fechaDate
        }

        val mes = calendar.get(Calendar.MONTH)
        val anio = calendar.get(Calendar.YEAR)

        "$mes-$anio"
    }

    val calendar = Calendar.getInstance()
    val mesActual = calendar.get(Calendar.MONTH)
    val anioActual = calendar.get(Calendar.YEAR)
    val claveActual = "$mesActual-$anioActual"

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

    fun obtenerNombreMes(clave: String): String {
        val partes = clave.split("-")
        val mes = partes[0].toInt()
        val anio = partes[1]

        val meses = listOf(
            "Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio", "Agosto",
            "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )

        return "${meses[mes]} $anio"
    }

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

        item { Spacer(modifier = Modifier.height(24.dp)) }

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
                        Text(formatearMontoApp(totalIngresosMesActual), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Balance estimado", style = MaterialTheme.typography.labelMedium, color = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                        Text(formatearMontoApp(balanceEstimadoMesActual), style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = if (balanceEstimadoMesActual >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer)
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

        cuentasPorMes.toSortedMap(compareByDescending { it }).forEach { (clave, cuentas) ->

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

        item {
            Spacer(modifier = Modifier.height(20.dp))
            EstadisticasPagos(listaCuentas, listaIngresos)
        }
        }
    }
}
