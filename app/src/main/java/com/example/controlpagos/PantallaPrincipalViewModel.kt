package com.example.controlpagos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.controlpagos.model.Cuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PantallaPrincipalUiState(
    val listaCuentas: List<Cuenta> = emptyList(),
    val cuentasFiltradasLista: List<Cuenta> = emptyList(),
    val nombre: String = "",
    val numeroCuenta: String = "",
    val monto: String = "",
    val fecha: String = "",
    val cuentaEditando: Cuenta? = null,
    val mostrarCalendario: Boolean = false,
    val cuentaAEliminar: Cuenta? = null,
    val textoBuscadorLista: String = "",
    val filtroSeleccionadoLista: String = "nombre",
    val cuentaAEliminarLista: Cuenta? = null,
    val mensajeSistema: String? = null
)

class PantallaPrincipalViewModel(
    private val cuentaRepository: CuentaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantallaPrincipalUiState())
    val uiState: StateFlow<PantallaPrincipalUiState> = _uiState.asStateFlow()

    init {
        cargarCuentas()
    }

    fun cargarCuentas() {
        viewModelScope.launch {
            val cuentas = cuentaRepository.obtenerCuentas()
            _uiState.update { estado ->
                estado.copy(
                    listaCuentas = cuentas,
                    cuentasFiltradasLista = filtrarCuentas(
                        cuentas = cuentas,
                        texto = estado.textoBuscadorLista,
                        filtro = estado.filtroSeleccionadoLista
                    )
                )
            }
        }
    }

    fun insertarCuenta(cuenta: Cuenta) {
        viewModelScope.launch {
            cuentaRepository.insertarCuenta(cuenta)
            cargarCuentas()
        }
    }

    fun actualizarCuenta(cuenta: Cuenta) {
        viewModelScope.launch {
            cuentaRepository.actualizarCuenta(cuenta)
            cargarCuentas()
        }
    }

    fun eliminarCuenta(cuenta: Cuenta) {
        viewModelScope.launch {
            cuentaRepository.eliminarCuenta(cuenta)
            cargarCuentas()
        }
    }

    fun importarCuentas(cuentas: List<Cuenta>) {
        viewModelScope.launch {
            var insertadas = 0
            cuentas.forEach { cuenta ->
                runCatching {
                    cuentaRepository.insertarCuenta(cuenta)
                    insertadas++
                }
            }
            cargarCuentas()
            _uiState.update { it.copy(mensajeSistema = "Importacion completada: $insertadas cuentas") }
        }
    }

    fun onNombreChange(valor: String) {
        _uiState.update { it.copy(nombre = valor) }
    }

    fun onNumeroCuentaChange(valor: String) {
        _uiState.update { it.copy(numeroCuenta = valor) }
    }

    fun onMontoChange(valor: String) {
        _uiState.update { it.copy(monto = valor) }
    }

    fun onFechaChange(valor: String) {
        _uiState.update { it.copy(fecha = valor) }
    }

    fun iniciarEdicion(cuenta: Cuenta) {
        _uiState.update {
            it.copy(
                nombre = cuenta.nombre,
                numeroCuenta = cuenta.numeroCuenta,
                monto = cuenta.monto.toString(),
                fecha = cuenta.fecha,
                cuentaEditando = cuenta
            )
        }
    }

    fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                numeroCuenta = "",
                monto = "",
                fecha = "",
                cuentaEditando = null
            )
        }
    }

    fun esFormularioValido(): Boolean {
        val estado = _uiState.value
        return estado.nombre.isNotBlank() &&
            estado.numeroCuenta.isNotBlank() &&
            ((estado.monto.toDoubleOrNull() ?: 0.0) > 0) &&
            estado.fecha.isNotBlank()
    }

    fun abrirCalendario() {
        _uiState.update { it.copy(mostrarCalendario = true) }
    }

    fun cerrarCalendario() {
        _uiState.update { it.copy(mostrarCalendario = false) }
    }

    fun solicitarEliminar(cuenta: Cuenta) {
        _uiState.update { it.copy(cuentaAEliminar = cuenta) }
    }

    fun cancelarEliminar() {
        _uiState.update { it.copy(cuentaAEliminar = null) }
    }

    fun onTextoBuscadorListaChange(valor: String) {
        _uiState.update { estado ->
            estado.copy(
                textoBuscadorLista = valor,
                cuentasFiltradasLista = filtrarCuentas(
                    cuentas = estado.listaCuentas,
                    texto = valor,
                    filtro = estado.filtroSeleccionadoLista
                )
            )
        }
    }

    fun limpiarBuscadorLista() {
        _uiState.update { estado ->
            estado.copy(
                textoBuscadorLista = "",
                cuentasFiltradasLista = filtrarCuentas(
                    cuentas = estado.listaCuentas,
                    texto = "",
                    filtro = estado.filtroSeleccionadoLista
                )
            )
        }
    }

    fun onFiltroSeleccionadoListaChange(valor: String) {
        _uiState.update { estado ->
            estado.copy(
                filtroSeleccionadoLista = valor,
                cuentasFiltradasLista = filtrarCuentas(
                    cuentas = estado.listaCuentas,
                    texto = estado.textoBuscadorLista,
                    filtro = valor
                )
            )
        }
    }

    fun solicitarEliminarLista(cuenta: Cuenta) {
        _uiState.update { it.copy(cuentaAEliminarLista = cuenta) }
    }

    fun cancelarEliminarLista() {
        _uiState.update { it.copy(cuentaAEliminarLista = null) }
    }

    fun limpiarMensajeSistema() {
        _uiState.update { it.copy(mensajeSistema = null) }
    }

    private fun filtrarCuentas(cuentas: List<Cuenta>, texto: String, filtro: String): List<Cuenta> {
        if (texto.isBlank()) return cuentas

        return cuentas.filter { cuenta ->
            when (filtro) {
                "nombre" -> cuenta.nombre.contains(texto, ignoreCase = true)
                "cuenta" -> cuenta.numeroCuenta.contains(texto, ignoreCase = true)
                else -> true
            }
        }
    }
}

class PantallaPrincipalViewModelFactory(
    private val cuentaRepository: CuentaRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PantallaPrincipalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PantallaPrincipalViewModel(cuentaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

