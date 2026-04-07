package com.example.controlpagos

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val PATRON_FECHA_APP = "dd/MM/yyyy"

fun parseFechaApp(valor: String): Date? {
    val formatter = SimpleDateFormat(PATRON_FECHA_APP, Locale.getDefault())
    formatter.isLenient = false
    return runCatching { formatter.parse(valor) }.getOrNull()
}

fun formatearFechaDesdeMillisUtc(millis: Long): String {
    val formatter = SimpleDateFormat(PATRON_FECHA_APP, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

fun formatearMontoApp(monto: Double): String {
    return String.format(Locale.US, "$%,.2f", monto)
}

