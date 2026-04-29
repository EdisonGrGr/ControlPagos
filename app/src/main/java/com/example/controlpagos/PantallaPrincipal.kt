package com.example.controlpagos

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Cuenta
import java.util.Calendar
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaPrincipal(viewModel: PantallaPrincipalViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val listaCuentas = uiState.listaCuentas
    val totalPendiente = listaCuentas.filter { !it.pagada }.sumOf { it.monto }
    val totalTexto = formatearMontoApp(totalPendiente)
    val cuentasPendientes = listaCuentas.count { !it.pagada }
    val cuentasPagadas = listaCuentas.count { it.pagada }

    val context = LocalContext.current
    val recordatorioHelper = RecordatorioHelper(context)
    var mostrarSelectorFecha by remember { mutableStateOf(false) }

    LaunchedEffect(mostrarSelectorFecha) {
        if (mostrarSelectorFecha) {
            val fechaInicial = parseFechaApp(uiState.fecha) ?: Date()
            val calendario = Calendar.getInstance().apply { time = fechaInicial }

            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val calendarioSeleccionado = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    viewModel.onFechaChange(formatearFechaApp(calendarioSeleccionado.time))
                    mostrarSelectorFecha = false
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setOnCancelListener { mostrarSelectorFecha = false }
                show()
            }
        }
    }

    val hoy = Date()

    val isFormValid = viewModel.esFormularioValido()
    val numeroCuentaDuplicado = remember(
        uiState.listaCuentas,
        uiState.numeroCuenta,
        uiState.fecha,
        uiState.cuentaEditando
    ) {
        viewModel.esNumeroCuentaDuplicadoEnMes(
            numeroCuenta = uiState.numeroCuenta,
            fecha = uiState.fecha,
            idExcluir = uiState.cuentaEditando?.id
        )
    }

    var filtroEstado by rememberSaveable { mutableStateOf("todas") }
    val listaCuentasFiltradas = remember(listaCuentas, filtroEstado) {
        when (filtroEstado) {
            "pendientes" -> listaCuentas.filter { !it.pagada }
            "pagadas" -> listaCuentas.filter { it.pagada }
            else -> listaCuentas
        }
    }

    val guardarCuenta: () -> Unit = guardarCuenta@{
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
            val cuentaAnterior = uiState.cuentaEditando ?: return@guardarCuenta
            recordatorioHelper.cancelarRecordatorio(cuentaAnterior.numeroCuenta, cuentaAnterior.fecha)

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
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

        item {
            ResumenPrincipalCard(
                totalTexto = totalTexto,
                cuentasPendientes = cuentasPendientes,
                cuentasPagadas = cuentasPagadas,
                enEdicion = uiState.cuentaEditando != null
            )
        }

        item {
            FormularioCuentaCard(
                uiState = uiState,
                numeroCuentaDuplicado = numeroCuentaDuplicado,
                isFormValid = isFormValid,
                onNombreChange = viewModel::onNombreChange,
                onNumeroCuentaChange = viewModel::onNumeroCuentaChange,
                onMontoChange = viewModel::onMontoChange,
                onFechaClick = { mostrarSelectorFecha = true },
                onGuardar = guardarCuenta
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Cuentas registradas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "${listaCuentasFiltradas.size} registros",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtroEstado == "todas",
                    onClick = { filtroEstado = "todas" },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = filtroEstado == "pendientes",
                    onClick = { filtroEstado = "pendientes" },
                    label = { Text("Pendientes") }
                )
                FilterChip(
                    selected = filtroEstado == "pagadas",
                    onClick = { filtroEstado = "pagadas" },
                    label = { Text("Pagadas") }
                )
            }
        }

        if (listaCuentasFiltradas.isEmpty()) {
            item {
                EstadoVacioCard()
            }
        }

        items(listaCuentasFiltradas, key = { it.id }) { cuenta ->

            CuentaCard(
                cuenta = cuenta,
                hoy = hoy,
                onTogglePago = {
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
                onEditar = { viewModel.iniciarEdicion(cuenta) },
                onEliminar = { viewModel.solicitarEliminar(cuenta) }
            )
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
                        recordatorioHelper.cancelarRecordatorio(it.numeroCuenta, it.fecha)
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
}


@Composable
private fun ResumenPrincipalCard(
    totalTexto: String,
    cuentasPendientes: Int,
    cuentasPagadas: Int,
    enEdicion: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Control de Pagos",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Visualiza y administra tus cuentas desde una sola vista.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (enEdicion) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Editando",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Total pendiente",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                totalTexto,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                InfoMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Pendientes",
                    value = cuentasPendientes.toString(),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
                InfoMetricCard(
                    modifier = Modifier.weight(1f),
                    label = "Pagadas",
                    value = cuentasPagadas.toString(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun InfoMetricCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColorFor(containerColor)
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = contentColorFor(containerColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioCuentaCard(
    uiState: PantallaPrincipalUiState,
    numeroCuentaDuplicado: Boolean,
    isFormValid: Boolean,
    onNombreChange: (String) -> Unit,
    onNumeroCuentaChange: (String) -> Unit,
    onMontoChange: (String) -> Unit,
    onFechaClick: () -> Unit,
    onGuardar: () -> Unit
) {
    val editando = uiState.cuentaEditando != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (editando) "Editar cuenta" else "Agregar cuenta",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (editando) {
                            "Actualiza la información y guarda los cambios."
                        } else {
                            "Completa los datos para registrar una nueva cuenta."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (editando) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Formulario activo",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.numeroCuenta,
                onValueChange = onNumeroCuentaChange,
                label = { Text("Número de cuenta") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                isError = numeroCuentaDuplicado && uiState.numeroCuenta.isNotBlank() && uiState.fecha.isNotBlank(),
                supportingText = {
                    if (numeroCuentaDuplicado && uiState.numeroCuenta.isNotBlank() && uiState.fecha.isNotBlank()) {
                        Text("Ese número de cuenta ya existe en el mismo mes")
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.monto,
                onValueChange = onMontoChange,
                label = { Text("Monto") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = uiState.fecha,
                onValueChange = {},
                label = { Text("Fecha de pago") },
                modifier = Modifier
                    .fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(18.dp),
                trailingIcon = {
                    IconButton(onClick = { onFechaClick() }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "Toca el campo para seleccionar la fecha de pago.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGuardar,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                enabled = isFormValid && !numeroCuentaDuplicado,
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (editando) "Actualizar cuenta" else "Guardar cuenta")
            }
        }
    }
}

@Composable
private fun EstadoVacioCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Aún no tienes cuentas visibles",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Usa el formulario de arriba para registrar tu primera cuenta y comenzar a darle seguimiento.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuentaCard(
    cuenta: Cuenta,
    hoy: Date,
    onTogglePago: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val fechaPago = parseFechaApp(cuenta.fecha)
    val vencido = !cuenta.pagada && fechaPago != null && fechaPago.before(hoy)
    val containerColor by animateColorAsState(
        targetValue = when {
            cuenta.pagada -> MaterialTheme.colorScheme.secondaryContainer
            vencido -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 350),
        label = "card_pago_color"
    )

    val badgeColor = when {
        cuenta.pagada -> MaterialTheme.colorScheme.tertiaryContainer
        vencido -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val badgeContentColor = contentColorFor(badgeColor)
    val textoPrincipalColor = when {
        cuenta.pagada -> MaterialTheme.colorScheme.onSecondaryContainer
        vencido -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    val inicial = cuenta.nombre.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(badgeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        inicial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeContentColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        cuenta.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = textoPrincipalColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Cuenta • ${cuenta.numeroCuenta}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textoPrincipalColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        "Fecha • ${cuenta.fecha}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (vencido) MaterialTheme.colorScheme.error else textoPrincipalColor.copy(alpha = 0.75f)
                    )
                }

                EstadoChip(
                    pagada = cuenta.pagada,
                    vencido = vencido,
                    fechaPago = cuenta.fechaPago
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "Monto",
                style = MaterialTheme.typography.labelLarge,
                color = textoPrincipalColor.copy(alpha = 0.8f)
            )

            Text(
                formatearMontoApp(cuenta.monto),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onTogglePago,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = if (cuenta.pagada) "Reabrir" else "Marcar pagada"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (cuenta.pagada) "Reabrir" else "Pagada")
                }

                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = onEliminar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EstadoChip(
    pagada: Boolean,
    vencido: Boolean,
    fechaPago: String?
) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(
                when {
                    pagada -> "Pagada${fechaPago?.let { " - $it" } ?: ""}"
                    vencido -> "Pendiente vencida"
                    else -> "Pendiente"
                }
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = when {
                pagada -> MaterialTheme.colorScheme.primaryContainer
                vencido -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            disabledLabelColor = when {
                pagada -> MaterialTheme.colorScheme.onPrimaryContainer
                vencido -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    )
}
