package com.example.controlpagos

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.controlpagos.model.Ingreso
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaIngresos(viewModel: IngresoViewModel) {
	val uiState by viewModel.uiState.collectAsState()
	val listaIngresos = uiState.listaIngresos
	val context = LocalContext.current
	var mostrarSelectorFecha by remember { mutableStateOf(false) }

	LaunchedEffect(mostrarSelectorFecha) {
		if (mostrarSelectorFecha) {
			val fechaInicial = parseFechaApp(uiState.fecha) ?: Date()
			val calendario = Calendar.getInstance().apply { time = fechaInicial }
			DatePickerDialog(
				context,
				{ _, year, month, dayOfMonth ->
					val seleccion = Calendar.getInstance().apply {
						set(Calendar.YEAR, year)
						set(Calendar.MONTH, month)
						set(Calendar.DAY_OF_MONTH, dayOfMonth)
						set(Calendar.HOUR_OF_DAY, 0)
						set(Calendar.MINUTE, 0)
						set(Calendar.SECOND, 0)
						set(Calendar.MILLISECOND, 0)
					}
					viewModel.onFechaChange(formatearFechaApp(seleccion.time))
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

	val guardarIngreso: () -> Unit = {
		if (uiState.ingresoEditando == null) {
			viewModel.insertarIngreso(
				Ingreso(
					concepto = uiState.concepto,
					categoria = uiState.categoria,
					monto = uiState.monto.toDoubleOrNull() ?: 0.0,
					fecha = uiState.fecha
				)
			)
		} else {
			uiState.ingresoEditando?.let { ingresoAnterior ->
				viewModel.actualizarIngreso(
					ingresoAnterior.copy(
						concepto = uiState.concepto,
						categoria = uiState.categoria,
						monto = uiState.monto.toDoubleOrNull() ?: 0.0,
						fecha = uiState.fecha
					)
				)
			}
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
						MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
						MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
					)
				)
			)
	) {
		LazyColumn(
			modifier = Modifier.fillMaxSize().padding(16.dp),
			contentPadding = PaddingValues(bottom = 100.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			item {
				Card(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(28.dp),
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
				) {
					Column(modifier = Modifier.padding(20.dp)) {
						Text("Ingresos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
						Spacer(modifier = Modifier.height(4.dp))
						Text("Registra las entradas de dinero y consulta el total disponible.", color = MaterialTheme.colorScheme.onPrimaryContainer)
						Spacer(modifier = Modifier.height(12.dp))
						Text("Total registrado: ${formatearMontoApp(listaIngresos.sumOf { it.monto })}", color = MaterialTheme.colorScheme.onPrimaryContainer)
					}
				}
			}

			item {
				Card(
					modifier = Modifier.fillMaxWidth(),
					shape = RoundedCornerShape(28.dp)
				) {
					Column(modifier = Modifier.padding(20.dp)) {
						Text(if (uiState.ingresoEditando == null) "Agregar ingreso" else "Editar ingreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
						Spacer(modifier = Modifier.height(16.dp))
						OutlinedTextField(uiState.concepto, viewModel::onConceptoChange, label = { Text("Concepto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp))
						Spacer(modifier = Modifier.height(10.dp))
						OutlinedTextField(uiState.categoria, viewModel::onCategoriaChange, label = { Text("Categoría") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp))
						Spacer(modifier = Modifier.height(10.dp))
						OutlinedTextField(uiState.monto, viewModel::onMontoChange, label = { Text("Monto") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp))
						Spacer(modifier = Modifier.height(10.dp))
						OutlinedTextField(
							value = uiState.fecha,
							onValueChange = {},
							label = { Text("Fecha") },
							modifier = Modifier.fillMaxWidth(),
							readOnly = true,
							shape = RoundedCornerShape(18.dp),
							trailingIcon = {
								IconButton(onClick = { mostrarSelectorFecha = true }) {
									Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
								}
							}
						)
						Spacer(modifier = Modifier.height(14.dp))
						Button(
							onClick = guardarIngreso,
							modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
							enabled = viewModel.esFormularioValido(),
							shape = RoundedCornerShape(16.dp)
						) {
							Text(if (uiState.ingresoEditando == null) "Guardar ingreso" else "Actualizar ingreso")
						}
					}
				}
			}

			item {
				Text("Ingresos registrados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
			}

			if (listaIngresos.isEmpty()) {
				item {
					Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
						Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
							Text("Aún no hay ingresos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
							Spacer(modifier = Modifier.height(6.dp))
							Text("Agrega tu primer ingreso para empezar a ver el balance.", color = MaterialTheme.colorScheme.onSurfaceVariant)
						}
					}
				}
			} else {
				items(listaIngresos, key = { it.id }) { ingreso ->
					Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
						Column(modifier = Modifier.padding(16.dp)) {
							Row(verticalAlignment = Alignment.Top) {
								Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer), contentAlignment = Alignment.Center) {
									Text(ingreso.concepto.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "I", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
								}
								Spacer(modifier = Modifier.width(12.dp))
								Column(modifier = Modifier.weight(1f)) {
									Text(ingreso.concepto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
									Text("Categoría • ${ingreso.categoria}", color = MaterialTheme.colorScheme.onSurfaceVariant)
									Text("Fecha • ${ingreso.fecha}", color = MaterialTheme.colorScheme.onSurfaceVariant)
								}
							}
							Spacer(modifier = Modifier.height(12.dp))
							Text(formatearMontoApp(ingreso.monto), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
							Spacer(modifier = Modifier.height(12.dp))
							Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
								OutlinedButton(onClick = { viewModel.iniciarEdicion(ingreso) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
									Icon(Icons.Default.Edit, contentDescription = "Editar")
									Spacer(modifier = Modifier.width(8.dp))
									Text("Editar")
								}
								OutlinedButton(onClick = { viewModel.solicitarEliminar(ingreso) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
									Icon(Icons.Default.Delete, contentDescription = "Eliminar")
									Spacer(modifier = Modifier.width(8.dp))
									Text("Eliminar")
								}
							}
						}
					}
				}
			}
		}
	}

	if (uiState.ingresoAEliminar != null) {
		AlertDialog(
			onDismissRequest = { viewModel.cancelarEliminar() },
			title = { Text("Eliminar ingreso") },
			text = { Text("¿Quieres eliminar este ingreso?") },
			confirmButton = {
				Button(onClick = {
					uiState.ingresoAEliminar?.let { viewModel.eliminarIngreso(it) }
					viewModel.cancelarEliminar()
				}) { Text("Eliminar") }
			},
			dismissButton = {
				TextButton(onClick = { viewModel.cancelarEliminar() }) { Text("Cancelar") }
			}
		)
	}
}

