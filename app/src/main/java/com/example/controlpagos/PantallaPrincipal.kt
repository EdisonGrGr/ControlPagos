package com.example.controlpagos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: PantallaPrincipalViewModel) {
    val datePickerState = rememberDatePickerState()

    val uiState by viewModel.uiState.collectAsState()
    val listaCuentas = uiState.listaCuentas
    val total = listaCuentas.sumOf { it.monto }
    val totalTexto = formatearMontoApp(total)

    val context = LocalContext.current
    val recordatorioHelper = RecordatorioHelper(context)

    val hoy = Date()

    val isFormValid = viewModel.esFormularioValido()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {

        item {
            Text(
                "Control de Pagos",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Text(
                "Registra y administra tus cuentas en un solo lugar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Agregar Cuenta", style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.numeroCuenta,
                        onValueChange = { viewModel.onNumeroCuentaChange(it) },
                        label = { Text("Número de cuenta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.monto,
                        onValueChange = { viewModel.onMontoChange(it) },
                        label = { Text("Monto") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.fecha,
                        onValueChange = {},
                        label = { Text("Fecha de pago") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FilledTonalButton(
                        onClick = { viewModel.abrirCalendario() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Seleccionar fecha")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (uiState.cuentaEditando == null) {
                                val nuevaCuenta = Cuenta(
                                    nombre = uiState.nombre,
                                    numeroCuenta = uiState.numeroCuenta,
                                    monto = uiState.monto.toDoubleOrNull() ?: 0.0,
                                    fecha = uiState.fecha
                                )

                                viewModel.insertarCuenta(nuevaCuenta)
                                recordatorioHelper.programarRecordatorio(
                                    uiState.nombre,
                                    uiState.numeroCuenta,
                                    uiState.fecha
                                )
                            } else {
                                val cuentaAnterior = uiState.cuentaEditando
                                    ?: return@Button
                                recordatorioHelper.cancelarRecordatorio(cuentaAnterior.numeroCuenta)

                                val cuentaActualizada = cuentaAnterior.copy(
                                    nombre = uiState.nombre,
                                    numeroCuenta = uiState.numeroCuenta,
                                    monto = uiState.monto.toDoubleOrNull() ?: 0.0,
                                    fecha = uiState.fecha
                                )

                                viewModel.actualizarCuenta(cuentaActualizada)
                                recordatorioHelper.programarRecordatorio(
                                    uiState.nombre,
                                    uiState.numeroCuenta,
                                    uiState.fecha
                                )
                            }

                            viewModel.limpiarFormulario()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid
                    ) {
                        Text(if (uiState.cuentaEditando == null) "Guardar Cuenta" else "Actualizar Cuenta")
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Total a pagar")

                    Text(
                        totalTexto,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        item {
            Text(
                "Cuentas Registradas",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }

        items(listaCuentas) { cuenta ->

            val fechaPago = parseFechaApp(cuenta.fecha)
            val vencido = fechaPago != null && fechaPago.before(hoy)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            cuenta.nombre,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("Cuenta: ${cuenta.numeroCuenta}")

                        Text("Monto: ${formatearMontoApp(cuenta.monto)}")

                        Text(
                            "Fecha: ${cuenta.fecha}",
                            color = if (vencido)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalIconButton(
                            onClick = {
                                viewModel.iniciarEdicion(cuenta)
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }

                        FilledTonalIconButton(
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            onClick = {
                                viewModel.solicitarEliminar(cuenta)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            }
        }
    }

    if (uiState.cuentaAEliminar != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelarEliminar() },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Estás seguro de que quieres eliminar esta cuenta?") },
            confirmButton = {
                Button(onClick = {
                    uiState.cuentaAEliminar?.let {
                        recordatorioHelper.cancelarRecordatorio(it.numeroCuenta)
                        viewModel.eliminarCuenta(it)
                    }
                    viewModel.cancelarEliminar()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelarEliminar() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (uiState.mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { viewModel.cerrarCalendario() },
            confirmButton = {
                Button(onClick = {

                    val millis = datePickerState.selectedDateMillis

                    if (millis != null) {
                        viewModel.onFechaChange(formatearFechaDesdeMillisUtc(millis))
                    }

                    viewModel.cerrarCalendario()

                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.cerrarCalendario() }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
