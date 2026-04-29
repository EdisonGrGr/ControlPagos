package com.example.controlpagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.model.Ingreso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngresoUiState(
	val listaIngresos: List<Ingreso> = emptyList(),
	val concepto: String = "",
	val categoria: String = "General",
	val monto: String = "",
	val fecha: String = "",
	val ingresoEditando: Ingreso? = null,
	val ingresoAEliminar: Ingreso? = null
)

class IngresoViewModel(
	private val ingresoRepository: IngresoRepository
) : ViewModel() {

	private val _uiState = MutableStateFlow(IngresoUiState())
	val uiState: StateFlow<IngresoUiState> = _uiState.asStateFlow()

	init {
		cargarIngresos()
	}

	fun cargarIngresos() {
		viewModelScope.launch {
			val ingresos = ingresoRepository.obtenerIngresos()
			_uiState.update { it.copy(listaIngresos = ingresos) }
		}
	}

	fun insertarIngreso(ingreso: Ingreso) {
		viewModelScope.launch {
			ingresoRepository.insertarIngreso(ingreso)
			cargarIngresos()
		}
	}

	fun actualizarIngreso(ingreso: Ingreso) {
		viewModelScope.launch {
			ingresoRepository.actualizarIngreso(ingreso)
			cargarIngresos()
		}
	}

	fun eliminarIngreso(ingreso: Ingreso) {
		viewModelScope.launch {
			ingresoRepository.eliminarIngreso(ingreso)
			cargarIngresos()
		}
	}

	fun iniciarEdicion(ingreso: Ingreso) {
		_uiState.update {
			it.copy(
				concepto = ingreso.concepto,
				categoria = ingreso.categoria,
				monto = ingreso.monto.toString(),
				fecha = ingreso.fecha,
				ingresoEditando = ingreso
			)
		}
	}

	fun limpiarFormulario() {
		_uiState.update {
			it.copy(
				concepto = "",
				categoria = "General",
				monto = "",
				fecha = "",
				ingresoEditando = null
			)
		}
	}

	fun onConceptoChange(valor: String) {
		_uiState.update { it.copy(concepto = valor) }
	}

	fun onCategoriaChange(valor: String) {
		_uiState.update { it.copy(categoria = valor) }
	}

	fun onMontoChange(valor: String) {
		_uiState.update { it.copy(monto = valor) }
	}

	fun onFechaChange(valor: String) {
		_uiState.update { it.copy(fecha = valor) }
	}

	fun solicitarEliminar(ingreso: Ingreso) {
		_uiState.update { it.copy(ingresoAEliminar = ingreso) }
	}

	fun cancelarEliminar() {
		_uiState.update { it.copy(ingresoAEliminar = null) }
	}

	fun esFormularioValido(): Boolean {
		val estado = _uiState.value
		return estado.concepto.isNotBlank() &&
			estado.categoria.isNotBlank() &&
			(estado.monto.toDoubleOrNull() ?: 0.0) > 0 &&
			estado.fecha.isNotBlank()
	}
}

class IngresoViewModelFactory(
	private val ingresoRepository: IngresoRepository
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(IngresoViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return IngresoViewModel(ingresoRepository) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}

