package com.example.controlpagos

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun PantallaListaPagosScreen(
    viewModel: PantallaPrincipalViewModel
) {
    val context = LocalContext.current
    val recordatorioHelper = RecordatorioHelper(context)
    val uiState by viewModel.uiState.collectAsState()
    val cuentasFiltradas = uiState.cuentasFiltradasLista
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val hoy = remember { Date() }
    var ordenSeleccionado by rememberSaveable { mutableStateOf("fechaAsc") }

    val totalTodas = uiState.listaCuentas.size
    val totalPendientes = uiState.listaCuentas.count { !it.pagada }
    val totalPagadas = uiState.listaCuentas.count { it.pagada }

    val cuentasOrdenadas = remember(cuentasFiltradas, ordenSeleccionado) {
        when (ordenSeleccionado) {
            "fechaDesc" -> cuentasFiltradas.sortedByDescending { parseFechaApp(it.fecha) ?: Date(Long.MIN_VALUE) }
            "montoAsc" -> cuentasFiltradas.sortedBy { it.monto }
            "montoDesc" -> cuentasFiltradas.sortedByDescending { it.monto }
            else -> cuentasFiltradas.sortedBy { parseFechaApp(it.fecha) ?: Date(Long.MAX_VALUE) }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.writer().use { writer ->
                    writer.write(cuentasToCsv(uiState.listaCuentas))
                }
            }
        }.onSuccess {
            scope.launch { snackBarHostState.showSnackbar("CSV exportado correctamente") }
        }.onFailure {
            scope.launch { snackBarHostState.showSnackbar("No se pudo exportar CSV") }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val csv = input.bufferedReader().use { it.readText() }
                val cuentas = csvToCuentas(csv)
                viewModel.importarCuentas(cuentas)
            }
        }.onFailure {
            scope.launch { snackBarHostState.showSnackbar("No se pudo importar CSV") }
        }
    }

    LaunchedEffect(uiState.mensajeSistema) {
        val mensaje = uiState.mensajeSistema ?: return@LaunchedEffect
        snackBarHostState.showSnackbar(mensaje)
        viewModel.limpiarMensajeSistema()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackBarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Lista de Pagos",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val nombre = "control_pagos_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
                        exportLauncher.launch(nombre)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Exportar CSV")
                }

                OutlinedButton(
                    onClick = {
                        importLauncher.launch(arrayOf("text/*", "text/csv", "application/csv"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Importar CSV")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = {
                    val total = uiState.listaCuentas.sumOf { it.monto }
                    val mensaje = buildString {
                        appendLine("Resumen Control de Pagos")
                        appendLine("Total cuentas: ${uiState.listaCuentas.size}")
                        appendLine("Total monto: ${formatearMontoApp(total)}")
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Resumen Control de Pagos")
                        putExtra(Intent.EXTRA_TEXT, mensaje)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir reporte"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Compartir reporte")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.textoBuscadorLista,
                onValueChange = { viewModel.onTextoBuscadorListaChange(it) },
                label = { Text("Buscar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (uiState.textoBuscadorLista.isNotEmpty()) {
                        IconButton(onClick = { viewModel.limpiarBuscadorLista() }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filtroSeleccionadoLista == "nombre",
                    onClick = { viewModel.onFiltroSeleccionadoListaChange("nombre") },
                    label = { Text("Nombre") }
                )
                FilterChip(
                    selected = uiState.filtroSeleccionadoLista == "cuenta",
                    onClick = { viewModel.onFiltroSeleccionadoListaChange("cuenta") },
                    label = { Text("Cuenta") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filtroEstadoLista == "todas",
                    onClick = {
                        viewModel.onFiltroEstadoListaChange("todas")
                    },
                    label = { Text("Todas ($totalTodas)") }
                )
                FilterChip(
                    selected = uiState.filtroEstadoLista == "pendientes",
                    onClick = {
                        viewModel.onFiltroEstadoListaChange("pendientes")
                    },
                    label = { Text("Pendientes ($totalPendientes)") }
                )
                FilterChip(
                    selected = uiState.filtroEstadoLista == "pagadas",
                    onClick = {
                        viewModel.onFiltroEstadoListaChange("pagadas")
                    },
                    label = { Text("Pagadas ($totalPagadas)") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = ordenSeleccionado == "fechaAsc",
                    onClick = { ordenSeleccionado = "fechaAsc" },
                    label = { Text("Fecha asc") }
                )
                FilterChip(
                    selected = ordenSeleccionado == "fechaDesc",
                    onClick = { ordenSeleccionado = "fechaDesc" },
                    label = { Text("Fecha desc") }
                )
                FilterChip(
                    selected = ordenSeleccionado == "montoAsc",
                    onClick = { ordenSeleccionado = "montoAsc" },
                    label = { Text("Monto asc") }
                )
                FilterChip(
                    selected = ordenSeleccionado == "montoDesc",
                    onClick = { ordenSeleccionado = "montoDesc" },
                    label = { Text("Monto desc") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(cuentasOrdenadas, key = { it.id }) { cuenta ->
                    val fechaPago = parseFechaApp(cuenta.fecha)
                    val vencido = !cuenta.pagada && fechaPago != null && fechaPago.before(hoy)
                    val colorCardAnimado by animateColorAsState(
                        targetValue = if (cuenta.pagada) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        animationSpec = tween(durationMillis = 350),
                        label = "lista_card_pago_color"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(containerColor = colorCardAnimado)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = cuenta.nombre,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text("Monto: ${formatearMontoApp(cuenta.monto)}")
                            Text("Numero de cuenta: ${cuenta.numeroCuenta}")
                            Text("Fecha: ${cuenta.fecha}")

                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = {
                                    Text(
                                        if (cuenta.pagada) {
                                            "Pagada${cuenta.fechaPago?.let { " - " + it } ?: ""}"
                                        } else {
                                            if (vencido) "Pendiente vencida" else "Pendiente"
                                        }
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    disabledContainerColor = when {
                                        cuenta.pagada -> MaterialTheme.colorScheme.primaryContainer
                                        vencido -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    disabledLabelColor = when {
                                        cuenta.pagada -> MaterialTheme.colorScheme.onPrimaryContainer
                                        vencido -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        if (cuenta.pagada) {
                                            viewModel.reabrirCuenta(cuenta)
                                            recordatorioHelper.programarRecordatorio(
                                                cuenta.nombre,
                                                cuenta.numeroCuenta,
                                                cuenta.fecha
                                            )
                                        } else {
                                            viewModel.marcarCuentaComoPagada(cuenta)
                                            recordatorioHelper.cancelarRecordatorio(cuenta.numeroCuenta, cuenta.fecha)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (cuenta.pagada) "Reabrir" else "Marcar pagada")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.solicitarEliminarLista(cuenta)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val cuentaAEliminar = uiState.cuentaAEliminarLista
    if (cuentaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminarLista() },
            title = { Text("Eliminar cuenta") },
            text = { Text("Estas seguro de que quieres eliminar esta cuenta?") },
            confirmButton = {
                Button(onClick = {
                    recordatorioHelper.cancelarRecordatorio(cuentaAEliminar.numeroCuenta, cuentaAEliminar.fecha)
                    viewModel.eliminarCuenta(cuentaAEliminar)
                    viewModel.cancelarEliminarLista()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminarLista() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

