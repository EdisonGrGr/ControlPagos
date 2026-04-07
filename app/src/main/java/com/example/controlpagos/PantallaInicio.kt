package com.example.controlpagos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import java.util.*

@Composable
fun PantallaInicio(listaCuentas: List<Cuenta>) {

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

    val totalMesActual = cuentasPorMes[claveActual]?.sumOf { it.monto } ?: 0.0

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
        .sortedBy { parseFechaApp(it.fecha) ?: Date(Long.MAX_VALUE) }
        .take(3)

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

                    Text(
                        text = "Total del mes actual",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = formatearMontoApp(totalMesActual),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            EstadisticasPagos(listaCuentas)
        }
    }
}
