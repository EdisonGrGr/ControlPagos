package com.example.controlpagos

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val PATRON_FECHA_APP = "dd/MM/yyyy"

fun parseFechaApp(valor: String): Date? {
    val formatter = SimpleDateFormat(PATRON_FECHA_APP, Locale.getDefault())
    formatter.isLenient = false
    return runCatching { formatter.parse(valor) }.getOrNull()
}


fun formatearFechaApp(fecha: Date): String {
    val formatter = SimpleDateFormat(PATRON_FECHA_APP, Locale.getDefault())
    return formatter.format(fecha)
}

fun formatearMontoApp(monto: Double): String {
    return String.format(Locale.US, "$%,.2f", monto)
}

