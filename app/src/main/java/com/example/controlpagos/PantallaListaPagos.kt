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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
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
    var filtroQuincena by rememberSaveable { mutableStateOf("todas") }
    val periodoActual = remember { periodoQuincenalDe(Date()) }

    val totalTodas = uiState.listaCuentas.size
    val totalPendientes = uiState.listaCuentas.count { !it.pagada }
    val totalPagadas = uiState.listaCuentas.count { it.pagada }

    val cuentasFiltradasQuincena = remember(cuentasFiltradas, filtroQuincena) {
        when (filtroQuincena) {
            "1" -> cuentasFiltradas.filter { periodoQuincenalDe(it.fecha)?.quincena == 1 }
            "2" -> cuentasFiltradas.filter { periodoQuincenalDe(it.fecha)?.quincena == 2 }
            else -> cuentasFiltradas
        }
    }

    val cuentasOrdenadas = remember(cuentasFiltradasQuincena, ordenSeleccionado) {
        when (ordenSeleccionado) {
            "fechaDesc" -> cuentasFiltradasQuincena.sortedByDescending { parseFechaApp(it.fecha) ?: Date(Long.MIN_VALUE) }
            "montoAsc" -> cuentasFiltradasQuincena.sortedBy { it.monto }
            "montoDesc" -> cuentasFiltradasQuincena.sortedByDescending { it.monto }
            else -> cuentasFiltradasQuincena.sortedBy { parseFechaApp(it.fecha) ?: Date(Long.MAX_VALUE) }
        }
    }
    val resultadosFiltrados = cuentasOrdenadas.size
    val resultadosPendientes = cuentasOrdenadas.count { !it.pagada }
    val resultadosPagadas = cuentasOrdenadas.count { it.pagada }
    val etiquetaQuincenaFiltro = when (filtroQuincena) {
        "1" -> "1ra quincena"
        "2" -> "2da quincena"
        else -> "Todas las quincenas"
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "Lista de Pagos",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Vista de pagos y quincenas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Filtra, ordena y revisa tus cuentas en una vista más clara.",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Filtro: $etiquetaQuincenaFiltro") },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("Quincena actual: ${etiquetaQuincenaCorta(periodoActual.quincena)}") },
                            colors = AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Mostrando", style = MaterialTheme.typography.labelMedium)
                                    Text(resultadosFiltrados.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Pendientes", style = MaterialTheme.typography.labelMedium)
                                    Text(resultadosPendientes.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Pagadas", style = MaterialTheme.typography.labelMedium)
                                    Text(resultadosPagadas.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            val nombre = "control_pagos_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.csv"
                            exportLauncher.launch(nombre)
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Exportar CSV") }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("text/*", "text/csv", "application/csv")) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Importar CSV") }
                }
            }

            item {
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
                ) { Text("Compartir reporte") }
            }

            item {
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
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
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
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = uiState.filtroEstadoLista == "todas", onClick = { viewModel.onFiltroEstadoListaChange("todas") }, label = { Text("Todas ($totalTodas)") })
                    FilterChip(selected = uiState.filtroEstadoLista == "pendientes", onClick = { viewModel.onFiltroEstadoListaChange("pendientes") }, label = { Text("Pendientes ($totalPendientes)") })
                    FilterChip(selected = uiState.filtroEstadoLista == "pagadas", onClick = { viewModel.onFiltroEstadoListaChange("pagadas") }, label = { Text("Pagadas ($totalPagadas)") })
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = filtroQuincena == "todas", onClick = { filtroQuincena = "todas" }, label = { Text("Todas las quincenas") })
                    FilterChip(selected = filtroQuincena == "1", onClick = { filtroQuincena = "1" }, label = { Text("1ra quincena") })
                    FilterChip(selected = filtroQuincena == "2", onClick = { filtroQuincena = "2" }, label = { Text("2da quincena") })
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = ordenSeleccionado == "fechaAsc", onClick = { ordenSeleccionado = "fechaAsc" }, label = { Text("Fecha asc") })
                    FilterChip(selected = ordenSeleccionado == "fechaDesc", onClick = { ordenSeleccionado = "fechaDesc" }, label = { Text("Fecha desc") })
                    FilterChip(selected = ordenSeleccionado == "montoAsc", onClick = { ordenSeleccionado = "montoAsc" }, label = { Text("Monto asc") })
                    FilterChip(selected = ordenSeleccionado == "montoDesc", onClick = { ordenSeleccionado = "montoDesc" }, label = { Text("Monto desc") })
                }
            }

            if (cuentasOrdenadas.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No hay cuentas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (uiState.listaCuentas.isEmpty()) {
                                    "Aún no hay cuentas registradas. Ve a 'Agregar' para crear una nueva."
                                } else {
                                    "No hay resultados que coincidan con los filtros aplicados."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(cuentasOrdenadas, key = { it.id }) { cuenta ->
                    val fechaPago = parseFechaApp(cuenta.fecha)
                    val vencido = !cuenta.pagada && fechaPago != null && fechaPago.before(hoy)
                    val colorCardAnimado by animateColorAsState(
                        targetValue = if (cuenta.pagada) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        animationSpec = tween(durationMillis = 350),
                        label = "lista_card_pago_color"
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = colorCardAnimado)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = cuenta.nombre, style = MaterialTheme.typography.titleLarge)
                            Text("Monto: ${formatearMontoApp(cuenta.monto)}")
                            Text("Número de cuenta: ${cuenta.numeroCuenta}")
                            Text("Fecha: ${cuenta.fecha}")
                            Spacer(modifier = Modifier.height(8.dp))

                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = {
                                    Text(
                                        if (cuenta.pagada) {
                                            "Pagada${cuenta.fechaPago?.let { " - $it" } ?: ""}"
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
                                            recordatorioHelper.programarRecordatorio(cuenta.nombre, cuenta.numeroCuenta, cuenta.fecha)
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
                                    onClick = { viewModel.solicitarEliminarLista(cuenta) },
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
            text = { Text("¿Estás seguro de que quieres eliminar esta cuenta?") },
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

